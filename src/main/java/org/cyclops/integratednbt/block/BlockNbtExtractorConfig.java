package org.cyclops.integratednbt.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.integratednbt.IntegratedNbt;

/**
 * Config for the NBT Extractor block.
 * @author rubensworks
 *
 */
public class BlockNbtExtractorConfig extends BlockConfig {

    public BlockNbtExtractorConfig() {
        super(
                IntegratedNbt._instance,
                "nbt_extractor",
                eConfig -> new BlockNbtExtractor(Block.Properties.of(Material.HEAVY_METAL)
                        .strength(5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedNbt._instance)
        );
    }

}
