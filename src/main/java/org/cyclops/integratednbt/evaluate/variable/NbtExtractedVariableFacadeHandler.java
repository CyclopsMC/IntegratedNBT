package org.cyclops.integratednbt.evaluate.variable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandler;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import org.cyclops.integratednbt.Reference;

import java.util.Optional;

public class NbtExtractedVariableFacadeHandler
    implements IVariableFacadeHandler<NbtExtractedVariableFacade> {
    private static final String KEY_SOURCE_NBT_ID = "sourceNBTId";
    private static final String KEY_EXTRACTION_PATH = "extractionPath";
    private static final String KEY_DEFAULT_NBT_ID = "defaultNBTId";
    private static NbtExtractedVariableFacadeHandler instance;

    public static NbtExtractedVariableFacadeHandler getInstance() {
        if (instance == null) {
            instance = new NbtExtractedVariableFacadeHandler();
        }
        return instance;
    }

    @Override
    public Identifier getUniqueName() {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "nbt_extracted");
    }

    @Override
    public NbtExtractedVariableFacade getVariableFacade(ValueDeseralizationContext deserializationContext, int id, CompoundTag tag) {
        int sourceNBTId = tag.getIntOr(KEY_SOURCE_NBT_ID, 0);
        Optional<SegmentedNbtPath> extractionPath = SegmentedNbtPath.fromNBT(tag.get(KEY_EXTRACTION_PATH));
        byte defaultNBTId = tag.getByteOr(KEY_DEFAULT_NBT_ID, (byte) 1);
        return new NbtExtractedVariableFacade(
            id,
            sourceNBTId,
            extractionPath.orElse(null),
            defaultNBTId
        );
    }

    @Override
    public void setVariableFacade(ValueDeseralizationContext valueDeseralizationContext, CompoundTag tag, NbtExtractedVariableFacade facade) {
        tag.putInt(KEY_SOURCE_NBT_ID, facade.getSourceNbtId());
        tag.put(KEY_EXTRACTION_PATH, facade.getExtractionPath().toNBT());
        tag.putByte(KEY_DEFAULT_NBT_ID, facade.getDefaultNbtId());
    }

    @Override
    public boolean isInstance(IVariableFacade variableFacade) {
        return variableFacade instanceof NbtExtractedVariableFacade;
    }

    @Override
    public boolean isInstance(IVariable<?> variable) {
        return variable instanceof NbtExtractedVariable;
    }
}
