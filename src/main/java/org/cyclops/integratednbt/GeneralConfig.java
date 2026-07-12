package org.cyclops.integratednbt;

import org.cyclops.cyclopscore.config.ConfigurablePropertyCommon;
import org.cyclops.cyclopscore.config.ModConfigLocation;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfigCommon;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfigCommon<IntegratedNbt> {

    @ConfigurablePropertyCommon(category = "general", comment = "The base energy usage for the NBT Extractor.", minimalValue = 0, configLocation = ModConfigLocation.SERVER)
    public static int nbtExtractorBaseConsumption = 2;

    public GeneralConfig() {
        super(IntegratedNbt._instance, "general");
    }

}
