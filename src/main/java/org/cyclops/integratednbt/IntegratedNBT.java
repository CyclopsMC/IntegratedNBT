package org.cyclops.integratednbt;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.config.ConfigHandler;
import org.cyclops.cyclopscore.init.ItemGroupMod;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.IClientProxy;
import org.cyclops.cyclopscore.proxy.ICommonProxy;
import org.cyclops.integratednbt.proxy.ClientProxy;
import org.cyclops.integratednbt.proxy.CommonProxy;

@Mod(Reference.MOD_ID)
public class IntegratedNBT extends ModBase<IntegratedNBT> {

    public static IntegratedNBT _instance;

    public IntegratedNBT() {
        super(Reference.MOD_ID, (instance) -> _instance = instance);

        // TODO: migrate code below...
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Additions.BLOCKS.register(modEventBus);
        Additions.ITEMS.register(modEventBus);
        Additions.CONTAINER_TYPES.register(modEventBus);
        Additions.BLOCK_ENTITIES.register(modEventBus);
    }

    @Override
    public CreativeModeTab constructDefaultCreativeModeTab() {
        return new ItemGroupMod(this, () -> RegistryEntries.ITEM_NBT_EXTRACTOR);
    }

    @Override
    public void onConfigsRegister(ConfigHandler configHandler) {
        super.onConfigsRegister(configHandler);

        configHandler.addConfigurable(new GeneralConfig());
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
        IntegratedNBT._instance.getLoggerHelper().log(level, message);
    }
}
