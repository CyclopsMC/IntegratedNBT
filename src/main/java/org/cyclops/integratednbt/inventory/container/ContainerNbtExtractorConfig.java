package org.cyclops.integratednbt.inventory.container;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.integratednbt.IntegratedNbt;

/**
 * Config for {@link ContainerNbtExtractor}.
 * @author rubensworks
 */
public class ContainerNbtExtractorConfig extends GuiConfigCommon<ContainerNbtExtractor, IntegratedNbt> {

    public ContainerNbtExtractorConfig() {
        super(IntegratedNbt._instance,
                "nbt_extractor",
                eConfig -> new ContainerTypeData<>((id, inv, data) -> new ContainerNbtExtractor(id, inv, (RegistryFriendlyByteBuf) data), FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerNbtExtractor> getScreenFactoryProvider() {
        return new ContainerNbtExtractorConfigScreenFactoryProvider();
    }

}
