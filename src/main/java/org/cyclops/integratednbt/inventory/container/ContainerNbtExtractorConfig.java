package org.cyclops.integratednbt.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratednbt.IntegratedNbt;
import org.cyclops.integratednbt.client.gui.container.ContainerScreenNbtExtractor;

/**
 * Config for {@link ContainerNbtExtractor}.
 * @author rubensworks
 */
public class ContainerNbtExtractorConfig extends GuiConfig<ContainerNbtExtractor> {

    public ContainerNbtExtractorConfig() {
        super(IntegratedNbt._instance,
                "nbt_extractor",
                eConfig -> new ContainerTypeData<>(ContainerNbtExtractor::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerNbtExtractor>> MenuScreens.ScreenConstructor<ContainerNbtExtractor, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenNbtExtractor::new);
    }

}
