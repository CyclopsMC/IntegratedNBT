package org.cyclops.integratednbt;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.cyclops.integratednbt.block.BlockNbtExtractor;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;
import org.cyclops.integratednbt.inventory.container.ContainerNbtExtractor;
import org.cyclops.integratednbt.item.ItemNbtExtractorRemote;

/**
 * Referenced registry entries.
 * @author rubensworks
 */
public class RegistryEntries {

    public static final DeferredHolder<Item, Item> ITEM_NBT_EXTRACTOR = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integratednbt:nbt_extractor"));
    public static final DeferredHolder<Item, ItemNbtExtractorRemote> ITEM_NBT_EXTRACTOR_REMOTE = DeferredHolder.create(Registries.ITEM, ResourceLocation.parse("integratednbt:nbt_extractor_remote"));

    public static final DeferredHolder<net.minecraft.world.level.block.Block, BlockNbtExtractor> BLOCK_NBT_EXTRACTOR = DeferredHolder.create(Registries.BLOCK, ResourceLocation.parse("integratednbt:nbt_extractor"));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityNbtExtractor>> BLOCK_ENTITY_NBT_EXTRACTOR = DeferredHolder.create(Registries.BLOCK_ENTITY_TYPE, ResourceLocation.parse("integratednbt:nbt_extractor"));

    public static final DeferredHolder<MenuType<?>, MenuType<ContainerNbtExtractor>> CONTAINER_NBT_EXTRACTOR = DeferredHolder.create(Registries.MENU, ResourceLocation.parse("integratednbt:nbt_extractor"));

}
