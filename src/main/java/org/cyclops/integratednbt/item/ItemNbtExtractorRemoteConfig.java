package org.cyclops.integratednbt.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.integratednbt.IntegratedNbt;

/**
 * Config for the remote NBT extractor.
 * @author rubensworks
 */
public class ItemNbtExtractorRemoteConfig extends ItemConfigCommon<IntegratedNbt> {

    public ItemNbtExtractorRemoteConfig() {
        super(
                IntegratedNbt._instance,
                "nbt_extractor_remote",
                (eConfig, props) -> new ItemNbtExtractorRemote(props.stacksTo(1))
        );
    }

}
