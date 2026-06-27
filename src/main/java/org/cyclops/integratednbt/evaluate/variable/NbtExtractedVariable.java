package org.cyclops.integratednbt.evaluate.variable;

import net.minecraft.nbt.Tag;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.expression.VariableAdapter;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValueType;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import org.cyclops.integratednbt.evaluate.nbt.NbtValueConverter;

public class NbtExtractedVariable extends VariableAdapter<IValue> {
    private IVariable<ValueNbt> sourceNBTVariable;
    private SegmentedNbtPath extractionPath;
    private Tag cachedValue;
    private byte defaultNBTId;

    public NbtExtractedVariable(
        IVariable<ValueNbt> sourceNBTVariable,
        SegmentedNbtPath extractionPath,
        byte defaultNBTId
    ) {
        this.sourceNBTVariable = sourceNBTVariable;
        this.extractionPath = extractionPath;
        this.defaultNBTId = defaultNBTId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IValueType<IValue> getType() {
        try {
            this.ensureCachedValue();
            if (this.cachedValue == null) {
                return NbtValueConverter.getDefaultValue(this.defaultNBTId).getType();
            }
            return (IValueType<IValue>) NbtValueConverter.mapNBTToValueType(this.cachedValue);
        } catch (EvaluationException ex) {
            return NbtValueConverter.getDefaultValue(this.defaultNBTId).getType();
        }
    }

    private void ensureCachedValue() throws EvaluationException {
        if (this.cachedValue == null) {
            this.sourceNBTVariable.addInvalidationListener(this);
            this.cachedValue =
                this.extractionPath.extract(this.sourceNBTVariable.getValue()
                    .getRawValue()
                    .orElse(null));
        }
    }

    @Override
    public IValue getValue() throws EvaluationException {
        this.ensureCachedValue();
        if (this.cachedValue == null) {
            return NbtValueConverter.getDefaultValue(this.defaultNBTId);
        }
        return NbtValueConverter.mapNBTToValue(this.cachedValue);
    }

    @Override
    public void invalidate() {
        this.cachedValue = null;
        super.invalidate();
    }
}
