package org.cyclops.integratednbt.evaluate.nbt.path;

import net.minecraft.nbt.Tag;

/**
 * @author rubensworks
 */
public interface Segment {
    String getDisplayText();

    String getCompactDisplayText();

    Tag access(Tag parent);

    void buildCyclopsNBTPath(StringBuilder stringBuilder);
}
