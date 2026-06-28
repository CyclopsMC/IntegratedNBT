package org.cyclops.integratednbt;

import net.minecraftforge.fml.config.ModConfig;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    @ConfigurableProperty(category = "general", comment = "The base energy usage for the NBT Extractor.", minimalValue = 0, configLocation = ModConfig.Type.SERVER)
    public static int nbtExtractorBaseConsumption = 2;

    public GeneralConfig() {
        super(IntegratedNbt._instance, "general");
    }

}
