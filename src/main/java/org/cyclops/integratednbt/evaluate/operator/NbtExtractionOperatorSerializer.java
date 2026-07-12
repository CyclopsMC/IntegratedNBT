package org.cyclops.integratednbt.evaluate.operator;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperator;
import org.cyclops.integrateddynamics.api.evaluate.operator.IOperatorSerializer;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NbtExtractionOperatorSerializer implements IOperatorSerializer<NbtExtractionOperator> {
    @Override
    public boolean canHandle(IOperator operator) {
        return operator instanceof NbtExtractionOperator;
    }

    @Override
    public Identifier getUniqueName() {
        return NbtExtractionOperator.UNIQUE_NAME;
    }

    @Override
    public void serialize(ValueOutput valueOutput, NbtExtractionOperator operator) {
        valueOutput.store("path", ExtraCodecs.NBT, operator.getExtractionPath().toNBT());
        valueOutput.putByte("defaultNBTId", operator.getDefaultNBTId());
    }

    @Override
    public NbtExtractionOperator deserialize(ValueInput valueInput) throws EvaluationException {
        try {
            SegmentedNbtPath path = valueInput.read("path", ExtraCodecs.NBT)
                .flatMap(SegmentedNbtPath::fromNBT)
                .orElse(new SegmentedNbtPath());
            byte defaultNBTId = valueInput.getByteOr("defaultNBTId", (byte) 1);
            return new NbtExtractionOperator(path, defaultNBTId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(Component.literal(e.getMessage()));
        }
    }
}
