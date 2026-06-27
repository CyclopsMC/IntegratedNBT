package org.cyclops.integratednbt.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.NBTExtractorBE;

/**
 * Updates the auto refresh flag in the NBT Extractor.
 * @author rubensworks
 */
public class NbtExtractorUpdateAutoRefreshPacket extends PacketCodec {

    @CodecField
    private BlockPos blockPos;
    @CodecField
    private boolean autoRefresh;

    public NbtExtractorUpdateAutoRefreshPacket() {

    }

    public NbtExtractorUpdateAutoRefreshPacket(BlockPos blockPos, boolean autoRefresh) {
        this.blockPos = blockPos;
        this.autoRefresh = autoRefresh;
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
        BlockEntityHelpers.get(world, blockPos, NBTExtractorBE.class)
                .ifPresent(blockEntity -> blockEntity.updateAutoRefresh(autoRefresh));
    }

}
