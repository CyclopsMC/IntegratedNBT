package org.cyclops.integratednbt;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class Additions { // TODO: migrate to configs

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
        ForgeRegistries.ITEMS,
        Reference.MOD_ID
    );

    public static final RegistryObject<NBTExtractorRemote> NBT_EXTRACTOR_REMOTE = ITEMS.register(
        NBTExtractorRemote.REGISTRY_NAME,
        NBTExtractorRemote::new
    );

    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(
        ForgeRegistries.MENU_TYPES,
        Reference.MOD_ID
    );

    public static final RegistryObject<MenuType<NBTExtractorContainer>> NBT_EXTRACTOR_CONTAINER = CONTAINER_TYPES
        .register(
            "nbt_extractor",
            () -> IForgeMenuType.create(NBTExtractorContainer::new)
        );

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITY_TYPES,
        Reference.MOD_ID
    );

    public static final RegistryObject<BlockEntityType<NBTExtractorBE>> NBT_EXTRACTOR_BE = BLOCK_ENTITIES.register(
            "nbt_extractor",
        () -> BlockEntityType.Builder.of(NBTExtractorBE::new, RegistryEntries.BLOCK_NBT_EXTRACTOR).build(null)
    );
//    public static final RegistryObject<BlockEntityType<?>> TILE_ENTITY_TYPES =
//        DeferredRegister.create(
//            ForgeRegistries.BLOCK_ENTITIES,
//            Reference.MOD_ID
//        );
//
//    @SuppressWarnings("ConstantConditions")
//    public static final RegistryObject<BlockEntityType<NBTExtractorBE>> NBT_EXTRACTOR_TILE_ENTITY = TILE_ENTITY_TYPES
//        .register(NBTExtractor.REGISTRY_NAME, () ->
//            BlockEntityType.Builder.of(NBTExtractorBE::new, NBT_EXTRACTOR_BLOCK.get())
//                .build(null));
}
