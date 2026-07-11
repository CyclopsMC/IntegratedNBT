package org.cyclops.integratednbt.helpers;

import net.minecraft.world.item.ItemStack;
import org.cyclops.integrateddynamics.RegistryEntries;

public class VariableHelpers {
    public static boolean isVariable(ItemStack itemStack) {
        return itemStack.getItem() == RegistryEntries.ITEM_VARIABLE.get();
    }
}
