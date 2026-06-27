package org.cyclops.integratednbt;

import org.cyclops.cyclopscore.config.extendedconfig.DummyConfig;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig extends DummyConfig {

    public GeneralConfig() {
        super(IntegratedNBT._instance, "general");
    }

}
