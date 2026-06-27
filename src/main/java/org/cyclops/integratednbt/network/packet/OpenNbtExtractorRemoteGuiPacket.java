package org.cyclops.integratednbt.network.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.Additions;
import org.cyclops.integratednbt.NBTExtractorRemote;

/**
 * Requests to open the GUI for an NBT Extractor at location.
 * @author rubensworks
 */
public class OpenNbtExtractorRemoteGuiPacket extends PacketCodec {

    public OpenNbtExtractorRemoteGuiPacket() {

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
        NBTExtractorRemote remote = Additions.NBT_EXTRACTOR_REMOTE.get();
        assert player != null;
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == remote) {
            remote.serverUse(player.getItemInHand(InteractionHand.MAIN_HAND), player);
        } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() == remote) {
            remote.serverUse(player.getItemInHand(InteractionHand.OFF_HAND), player);
        }
    }

}
