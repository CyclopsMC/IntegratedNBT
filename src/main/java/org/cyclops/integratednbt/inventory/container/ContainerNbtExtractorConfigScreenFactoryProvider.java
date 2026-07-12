package org.cyclops.integratednbt.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.integratednbt.client.gui.container.ContainerScreenNbtExtractor;

/**
 * @author rubensworks
 */
public class ContainerNbtExtractorConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerNbtExtractor> {
    @Override
    public <U extends Screen & MenuAccess<ContainerNbtExtractor>> MenuScreens.ScreenConstructor<ContainerNbtExtractor, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenNbtExtractor::new);
    }
}
