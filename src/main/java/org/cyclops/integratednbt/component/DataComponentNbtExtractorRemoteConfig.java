package org.cyclops.integratednbt.component;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.integratednbt.IntegratedNbt;
/**
 * Config for the NBT extractor remote binding data component.
 * @author rubensworks
 */
public class DataComponentNbtExtractorRemoteConfig extends DataComponentConfigCommon<NbtExtractorRemoteBoundData, IntegratedNbt> {
    public DataComponentNbtExtractorRemoteConfig() {
        super(IntegratedNbt._instance, "nbt_extractor_remote", builder -> builder
                .persistent(NbtExtractorRemoteBoundData.CODEC)
                .networkSynchronized(NbtExtractorRemoteBoundData.STREAM_CODEC));
    }
}
