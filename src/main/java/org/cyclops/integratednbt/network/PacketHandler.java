package org.cyclops.integratednbt.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.cyclops.integratednbt.Reference;
import org.cyclops.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage.NBTExtractorUpdateClientMessageHandler;
import org.cyclops.integratednbt.network.serverbound.NBTExtractorRemoteRequestMessage.NBTExtractorRemoteRequestMessageHandler;
import org.cyclops.integratednbt.network.serverbound.NBTExtractorUpdateAutoRefreshMessage.NBTExtractorUpdateAutoRefreshMessageHandler;
import org.cyclops.integratednbt.network.serverbound.NBTExtractorUpdateExtractionPathMessage.NBTExtractorUpdateExtractionPathMessageHandler;
import org.cyclops.integratednbt.network.serverbound.NBTExtractorUpdateOutputModeMessage.NBTExtractorUpdateOutputModeMessageHandler;

public abstract class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(Reference.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = -1;
        new NBTExtractorUpdateClientMessageHandler().register(INSTANCE, ++id);
        new NBTExtractorRemoteRequestMessageHandler().register(INSTANCE, ++id);
        new NBTExtractorUpdateAutoRefreshMessageHandler().register(INSTANCE, ++id);
        new NBTExtractorUpdateExtractionPathMessageHandler().register(INSTANCE, ++id);
        new NBTExtractorUpdateOutputModeMessageHandler().register(INSTANCE, ++id);
    }
}
