package org.cyclops.integratednbt.evaluate.variable;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.VariableFacadeBase;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import org.cyclops.integratednbt.evaluate.nbt.NbtValueConverter;

import javax.annotation.Nullable;
import java.util.List;

public class NbtExtractedVariableFacade extends VariableFacadeBase {
    private int sourceNbtId;
    private SegmentedNbtPath extractionPath;
    private byte defaultNbtId;
    private boolean validating;
    private boolean gettingVariable;
    private int lastNetworkHash;
    private NbtExtractedVariable variable;

    public NbtExtractedVariableFacade(
        boolean generateId,
        int sourceNbtId,
        @Nullable SegmentedNbtPath extractionPath,
        byte defaultNbtId
    ) {
        super(generateId);
        this.sourceNbtId = sourceNbtId;
        this.extractionPath = extractionPath;
        this.defaultNbtId = defaultNbtId;
    }

    public NbtExtractedVariableFacade(
        int id,
        int sourceNbtId,
        @Nullable SegmentedNbtPath extractionPath,
        byte defaultNbtId
    ) {
        super(id);
        this.sourceNbtId = sourceNbtId;
        this.extractionPath = extractionPath;
        this.defaultNbtId = defaultNbtId;
    }

    public byte getDefaultNbtId() {
        return this.defaultNbtId;
    }

    public int getSourceNbtId() {
        return this.sourceNbtId;
    }

    public SegmentedNbtPath getExtractionPath() {
        return this.extractionPath;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(List<Component> list, Level world) {
        if (!this.isValid()) {
            return;
        }
        list.add(Component.translatable(
            "integratednbt:nbt_extracted_variable.tooltip.source_nbt_id",
            this.sourceNbtId
        ));
        list.add(Component.translatable(
            "integratednbt:nbt_extracted_variable.tooltip.path",
            this.extractionPath.getDisplayText()
        ));
        list.add(Component.translatable(
            "integratednbt:nbt_extracted_variable.tooltip.default_value",
            NbtValueConverter.getDefaultValueDisplayText(this.defaultNbtId)
        ));
        super.appendHoverText(list, world);
    }

    @Override
    public boolean isValid() {
        return this.extractionPath != null;
    }

    @Override
    public void addModelOverlay(
        IVariableModelBaked variableModelBaked,
        List<BakedQuad> quads,
        RandomSource random,
        ModelData modelData
    ) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends IValue> IVariable<V> getVariable(
        INetwork network, IPartNetwork partNetwork
    ) {
        if (!this.isValid()) {
            return null;
        }
        int newNetworkHash = partNetwork != null ? partNetwork.hashCode() : -1;
        if (this.variable == null || newNetworkHash != this.lastNetworkHash) {
            this.lastNetworkHash = newNetworkHash;
            if (partNetwork == null || !partNetwork.hasVariableFacade(this.sourceNbtId)) {
                return null;
            }
            IVariableFacade sourceNbtVariableFacade = partNetwork.getVariableFacade(this.sourceNbtId);
            if (!sourceNbtVariableFacade.isValid() || sourceNbtVariableFacade == this) {
                return null;
            }
            if (this.gettingVariable) {
                return null;
            }
            this.gettingVariable = true;
            IVariable<ValueNbt> sourceNbtVariable = sourceNbtVariableFacade.getVariable(network, partNetwork);
            this.gettingVariable = false;
            if (sourceNbtVariable == null) {
                return null;
            }
            this.variable = new NbtExtractedVariable(
                sourceNbtVariable,
                this.extractionPath,
                this.defaultNbtId
            );
        }
        return (IVariable<V>) this.variable;
    }

    @Override
    public void validate(
            INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType
    ) {
        if (!this.isValid()) {
            return;
        }
        if (this.sourceNbtId < 0) {
            validator.addError(Component.translatable(L10NValues.VARIABLE_ERROR_INVALIDITEM));
        } else if (!partNetwork.hasVariableFacade(this.sourceNbtId)) {
            validator.addError(Component.translatable(
                L10NValues.OPERATOR_ERROR_VARIABLENOTINNETWORK,
                Integer.toString(this.sourceNbtId)
            ));
        } else {
            IVariableFacade sourceVariableFacade = partNetwork.getVariableFacade(this.sourceNbtId);
            if (sourceVariableFacade == this) {
                validator.addError(Component.translatable(
                    L10NValues.OPERATOR_ERROR_CYCLICREFERENCE,
                    Integer.toString(this.sourceNbtId)
                ));
            } else if (sourceVariableFacade != null) {
                final Wrapper<Boolean> isValid = new Wrapper<>(true);
                if (this.validating) {
                    validator.addError(Component.translatable(
                        L10NValues.OPERATOR_ERROR_CYCLICREFERENCE,
                        this.getId()
                    ));
                }
                this.validating = true;
                sourceVariableFacade.validate(network, partNetwork, error -> {
                    validator.addError(error);
                    isValid.set(false);
                }, ValueTypes.NBT);
                this.validating = false;
            }
        }
    }

    @Override
    public IValueType<?> getOutputType() {
        return ValueTypes.CATEGORY_ANY;
    }
}
