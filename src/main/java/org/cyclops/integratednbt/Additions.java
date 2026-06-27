package org.cyclops.integratednbt;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class Additions { // TODO: migrate to configs

    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(
        ForgeRegistries.MENU_TYPES,
        Reference.MOD_ID
    );

    public static final RegistryObject<MenuType<NBTExtractorContainer>> NBT_EXTRACTOR_CONTAINER = CONTAINER_TYPES
        .register(
            "nbt_extractor",
            () -> IForgeMenuType.create(NBTExtractorContainer::new)
        );

}
