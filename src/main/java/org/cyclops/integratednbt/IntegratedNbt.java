package org.cyclops.integratednbt;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.infobook.IInfoBookRegistry;
import org.cyclops.cyclopscore.init.ItemGroupMod;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorRegistry;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.infobook.OnTheDynamicsOfIntegrationBook;
import org.cyclops.integratednbt.block.BlockNbtExtractorConfig;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractorConfig;
import org.cyclops.integratednbt.client.model.VariableModelProviders;
import org.cyclops.integratednbt.evaluate.operator.NbtExtractionOperatorSerializer;
import org.cyclops.integratednbt.evaluate.variable.NbtExtractedVariableFacadeHandler;
import org.cyclops.integratednbt.inventory.container.ContainerNbtExtractorConfig;
import org.cyclops.integratednbt.item.ItemNbtExtractorRemoteConfig;
import org.cyclops.integratednbt.proxy.ClientProxy;
import org.cyclops.integratednbt.proxy.CommonProxy;

@Mod(Reference.MOD_ID)
public class IntegratedNbt extends ModBase<IntegratedNbt> {

    public static IntegratedNbt _instance;

    public IntegratedNbt() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {
        super.setup(event);

        event.enqueueWork(() -> {
            // Initialize info book
            IntegratedDynamics._instance.getRegistryManager().getRegistry(IInfoBookRegistry.class)
                    .registerSection(this,
                            OnTheDynamicsOfIntegrationBook.getInstance(), "info_book.integrateddynamics.manual",
                            "/data/" + Reference.MOD_ID + "/info/nbt_info.xml");
            IntegratedDynamics._instance.getRegistryManager().getRegistry(IInfoBookRegistry.class)
                    .registerSection(this,
                            OnTheDynamicsOfIntegrationBook.getInstance(), "info_book.integrateddynamics.tutorials",
                            "/data/" + Reference.MOD_ID + "/info/nbt_tutorials.xml");

            VariableFacadeHandlerRegistry.getInstance()
                    .registerHandler(new NbtExtractedVariableFacadeHandler());
            OperatorRegistry.getInstance()
                    .registerSerializer(new NbtExtractionOperatorSerializer());

            if (MinecraftHelpers.isClientSide()) {
                VariableModelProviders.load();
            }
        });
    }

    @Override
    public CreativeModeTab constructDefaultCreativeModeTab() {
        return new ItemGroupMod(this, () -> RegistryEntries.ITEM_NBT_EXTRACTOR);
    }

    @Override
    public void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig());

        configHandler.addConfigurable(new ItemNbtExtractorRemoteConfig());

        configHandler.addConfigurable(new BlockNbtExtractorConfig());

        configHandler.addConfigurable(new BlockEntityNbtExtractorConfig());

        configHandler.addConfigurable(new ContainerNbtExtractorConfig());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected IClientProxy constructClientProxy() {
        return new ClientProxy();
    }

    @Override
    protected ICommonProxy constructCommonProxy() {
        return new CommonProxy();
    }

    /**
     * Log a new info message for this mod.
     * @param message The message to show.
     */
    public static void clog(String message) {
        clog(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     * @param level The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void clog(Level level, String message) {
        IntegratedNbt._instance.getLoggerHelper().log(level, message);
    }
}
