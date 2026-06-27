package org.cyclops.integratednbt.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;
import org.cyclops.integrateddynamics.blockentity.BlockEntityCoalGenerator;
import org.cyclops.integratednbt.IntegratedNbt;
import org.cyclops.integratednbt.RegistryEntries;

/**
 * Config for the {@link BlockEntityCoalGenerator}.
 * @author rubensworks
 *
 */
public class BlockEntityNbtExtractorConfig extends BlockEntityConfig<BlockEntityNbtExtractor> {

    public BlockEntityNbtExtractorConfig() {
        super(
                IntegratedNbt._instance,
                "nbt_extractor",
                (eConfig) -> new BlockEntityType<>(BlockEntityNbtExtractor::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_NBT_EXTRACTOR), null)
        );
    }

}
