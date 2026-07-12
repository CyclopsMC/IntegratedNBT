package org.cyclops.integratednbt.evaluate.variable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.L10NValues;
import org.cyclops.integrateddynamics.core.item.ProxyVariableFacade;
import org.cyclops.integrateddynamics.core.item.VariableFacadeBase;
import org.cyclops.integratednbt.evaluate.nbt.NbtValueConverter;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class NbtExtractedVariableFacade extends VariableFacadeBase {
    private int sourceNbtId;
    private SegmentedNbtPath extractionPath;
    private byte defaultNbtId;
    private boolean isValidatingVariable = false;
    private boolean isGettingVariable = false;

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

    @Override
    protected IVariableFacadeClient constructClient() {
        return new NbtExtractedVariableFacadeClient(this);
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
    public void appendHoverText(Consumer<Component> tooltipAdder, Item.TooltipContext context) {
        if (!this.isValid()) {
            return;
        }
        tooltipAdder.accept(Component.translatable(
            "integratednbt:nbt_extracted_variable.tooltip.source_nbt_id",
            this.sourceNbtId
        ));
        tooltipAdder.accept(Component.translatable(
            "integratednbt:nbt_extracted_variable.tooltip.path",
            this.extractionPath.getDisplayText()
        ));
        tooltipAdder.accept(Component.translatable(
            "integratednbt:nbt_extracted_variable.tooltip.default_value",
            NbtValueConverter.getDefaultValueDisplayText(this.defaultNbtId)
        ));
        super.appendHoverText(tooltipAdder, context);
    }

    @Override
    public boolean isValid() {
        return this.extractionPath != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends IValue> IVariable<V> getVariable(INetwork network, IPartNetwork partNetwork) {
        if(isValid()) {
            // Check if we are entering an infinite recursion
            if (this.isGettingVariable) {
                throw new ProxyVariableFacade.VariableRecursionException("Detected infinite recursion for variable references.");
            }
            this.isGettingVariable = true;
            IVariableFacade sourceNbtVariableFacade = partNetwork.getVariableFacade(this.sourceNbtId);
            this.isGettingVariable = false;
            if (sourceNbtVariableFacade == null || !sourceNbtVariableFacade.isValid() || sourceNbtVariableFacade == this) {
                return null;
            }
            IVariable<ValueNbt> sourceNbtVariable = sourceNbtVariableFacade.getVariable(network, partNetwork);
            if (sourceNbtVariable == null) {
                return null;
            }
            return (IVariable<V>) new NbtExtractedVariable(
                    sourceNbtVariable,
                    this.extractionPath,
                    this.defaultNbtId
            );
        }
        return null;
    }

    @Override
    public void validate(INetwork network, IPartNetwork partNetwork, IValidator validator, IValueType containingValueType) {
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
                // Check if we are entering an infinite recursion
                if(this.isValidatingVariable) {
                    throw new ProxyVariableFacade.VariableRecursionException("Detected infinite recursion for variable references.");
                }
                this.isValidatingVariable = true;
                getVariable(network, partNetwork);
                this.isValidatingVariable = false;
            }
        }
    }

    @Override
    public IValueType<?> getOutputType() {
        return ValueTypes.CATEGORY_ANY;
    }
}
