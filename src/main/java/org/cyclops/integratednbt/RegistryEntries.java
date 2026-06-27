package org.cyclops.integratednbt;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import org.cyclops.integratednbt.block.BlockNbtExtractor;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;
import org.cyclops.integratednbt.item.ItemNbtExtractorRemote;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    @ObjectHolder(registryName = "item", value = "integratednbt:nbt_extractor")
    public static final Item ITEM_NBT_EXTRACTOR = null;
    @ObjectHolder(registryName = "item", value = "integratednbt:nbt_extractor_remote")
    public static final ItemNbtExtractorRemote ITEM_NBT_EXTRACTOR_REMOTE = null;

    @ObjectHolder(registryName = "block", value = "integratednbt:nbt_extractor")
    public static final BlockNbtExtractor BLOCK_NBT_EXTRACTOR = null;

    @ObjectHolder(registryName = "block_entity_type", value = "integratednbt:nbt_extractor")
    public static final BlockEntityType<BlockEntityNbtExtractor> BLOCK_ENTITY_NBT_EXTRACTOR = null;

}
