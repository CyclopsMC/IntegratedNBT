package org.cyclops.integratednbt.evaluate.operator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;

public class NbtExtractionOperatorSerializer implements IOperatorSerializer<NbtExtractionOperator> {
    @Override
    public boolean canHandle(IOperator operator) {
        return operator instanceof NbtExtractionOperator;
    }

    @Override
    public ResourceLocation getUniqueName() {
        return NbtExtractionOperator.UNIQUE_NAME;
    }

    @Override
    public Tag serialize(NbtExtractionOperator operator) {
        CompoundTag data = new CompoundTag();
        data.put("path", operator.getExtractionPath().toNBT());
        data.putByte("defaultNBTId", operator.getDefaultNBTId());
        return data;
    }

    @Override
    public NbtExtractionOperator deserialize(Tag nbt) throws EvaluationException {
        try {
            CompoundTag tag = (CompoundTag) nbt;
            return new NbtExtractionOperator(SegmentedNbtPath.fromNBT(tag.get("path"))
                .orElse(new SegmentedNbtPath()), tag.getByte("defaultNBTId"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(Component.literal(e.getMessage()));
        }
    }
}
