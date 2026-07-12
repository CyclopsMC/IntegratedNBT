package org.cyclops.integratednbt.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.integratednbt.IntegratedNbt;
import org.cyclops.integratednbt.RegistryEntries;

/**
 * Config for the {@link BlockEntityNbtExtractor}.
 * @author rubensworks
 *
 */
public class BlockEntityNbtExtractorConfig extends BlockEntityConfigCommon<BlockEntityNbtExtractor, IntegratedNbt> {

    public BlockEntityNbtExtractorConfig() {
        super(
                IntegratedNbt._instance,
                "nbt_extractor",
                (eConfig) -> new BlockEntityType<BlockEntityNbtExtractor>(BlockEntityNbtExtractor::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_NBT_EXTRACTOR.get()))
        );
        IntegratedNbt._instance.getModEventBus().addListener(new BlockEntityNbtExtractor.CapabilityRegistrar(this::getInstance)::register);
    }

}
