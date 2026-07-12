package org.cyclops.integratednbt.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.Reference;
import org.cyclops.integratednbt.RegistryEntries;
import org.cyclops.integratednbt.item.ItemNbtExtractorRemote;

/**
 * Requests to open the GUI for an NBT Extractor at location.
 * @author rubensworks
 */
public class OpenNbtExtractorRemoteGuiPacket extends PacketCodec<OpenNbtExtractorRemoteGuiPacket> {

    public static final CustomPacketPayload.Type<OpenNbtExtractorRemoteGuiPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "open_nbt_extractor_remote_gui"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenNbtExtractorRemoteGuiPacket> CODEC = getCodec(OpenNbtExtractorRemoteGuiPacket::new);

    public OpenNbtExtractorRemoteGuiPacket() {
        super(TYPE);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf output) {
        super.encode(output);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf input) {
        super.decode(input);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        // Do nothing
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        ItemNbtExtractorRemote remote = RegistryEntries.ITEM_NBT_EXTRACTOR_REMOTE.get();
        assert player != null;
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == remote) {
            remote.serverUse(player.getItemInHand(InteractionHand.MAIN_HAND), player);
        } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() == remote) {
            remote.serverUse(player.getItemInHand(InteractionHand.OFF_HAND), player);
        }
    }

}
