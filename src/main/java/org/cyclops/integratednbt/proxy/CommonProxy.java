package org.cyclops.integratednbt.proxy;

import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;
import org.cyclops.integratednbt.IntegratedNbt;
import org.cyclops.integratednbt.network.packet.*;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return IntegratedNbt._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);

        // Register packets.
        packetHandler.register(UpdateClientNbtExtractorPacket.class, UpdateClientNbtExtractorPacket.TYPE, UpdateClientNbtExtractorPacket.CODEC);
        packetHandler.register(OpenNbtExtractorRemoteGuiPacket.class, OpenNbtExtractorRemoteGuiPacket.TYPE, OpenNbtExtractorRemoteGuiPacket.CODEC);
        packetHandler.register(NbtExtractorUpdateAutoRefreshPacket.class, NbtExtractorUpdateAutoRefreshPacket.TYPE, NbtExtractorUpdateAutoRefreshPacket.CODEC);
        packetHandler.register(NbtExtractorSetExtractionPathPacket.class, NbtExtractorSetExtractionPathPacket.TYPE, NbtExtractorSetExtractionPathPacket.CODEC);
        packetHandler.register(NbtExtractorSetOutputModePacket.class, NbtExtractorSetOutputModePacket.TYPE, NbtExtractorSetOutputModePacket.CODEC);
    }

}
