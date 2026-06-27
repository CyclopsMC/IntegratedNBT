package org.cyclops.integratednbt.evaluate.nbt.path;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * @author rubensworks
 */
public class IndexSegment implements Segment {
    private final int index;

    public IndexSegment(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        IndexSegment that = (IndexSegment) o;

        return this.index == that.index;
    }

    @Override
    public int hashCode() {
        return this.index;
    }

    @Override
    public String getDisplayText() {
        return I18n.get(
                "integratednbt:nbt_extractor.index",
                String.valueOf(index)
        );
    }

    @Override
    public String getCompactDisplayText() {
        return "[" + this.index + "]";
    }

    @Override
    public Tag access(Tag parent) {
        if (parent instanceof ListTag) {
            ListTag parentList = ((ListTag) parent);
            if (parentList.size() <= this.index || this.index < 0) {
                return null;
            }
            Tag base = parentList.get(this.index);
            if (base.getId() == 0 /* TagEnd */) {
                return null;
            }
            return base;
        } else {
            return null;
        }
    }

    @Override
    public void buildCyclopsNBTPath(StringBuilder stringBuilder) {
        stringBuilder.append("[").append(this.index).append("]");
    }
}
