package org.cyclops.integratednbt.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integratednbt.IntegratedNBT;
import org.cyclops.integratednbt.network.packet.*;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return IntegratedNBT._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);

        // Register packets.
        packetHandler.register(UpdateClientNbtExtractorPacket.class);
        packetHandler.register(OpenNbtExtractorRemoteGuiPacket.class);
        packetHandler.register(NbtExtractorUpdateAutoRefreshPacket.class);
        packetHandler.register(NbtExtractorSetExtractionPathPacket.class);
        packetHandler.register(NbtExtractorSetOutputModePacket.class);
    }

}
