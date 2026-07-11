package org.cyclops.integratednbt.item;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;
import org.cyclops.integratednbt.IntegratedNbt;

/**
 * Config for the remote NBT extractor.
 * @author rubensworks
 */
public class ItemNbtExtractorRemoteConfig extends ItemConfig {

    public ItemNbtExtractorRemoteConfig() {
        super(
                IntegratedNbt._instance,
                "nbt_extractor_remote",
                eConfig -> new ItemNbtExtractorRemote(new Item.Properties()
                        .stacksTo(1))
        );
    }

}
