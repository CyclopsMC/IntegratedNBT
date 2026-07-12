package org.cyclops.integratednbt.block;

import net.minecraft.world.level.block.SoundType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.integratednbt.IntegratedNbt;

/**
 * Config for the NBT Extractor block.
 * @author rubensworks
 *
 */
public class BlockNbtExtractorConfig extends BlockConfigCommon<IntegratedNbt> {

    public BlockNbtExtractorConfig() {
        super(
                IntegratedNbt._instance,
                "nbt_extractor",
                (eConfig, props) -> new BlockNbtExtractor(props
                        .strength(5.0F)
                        .sound(SoundType.METAL)),
                getDefaultItemConstructor(IntegratedNbt._instance)
        );
    }

}
