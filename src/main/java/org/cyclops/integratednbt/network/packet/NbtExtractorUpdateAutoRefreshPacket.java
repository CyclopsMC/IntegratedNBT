package org.cyclops.integratednbt.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.Reference;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;

/**
 * Updates the auto refresh flag in the NBT Extractor.
 * @author rubensworks
 */
public class NbtExtractorUpdateAutoRefreshPacket extends PacketCodec<NbtExtractorUpdateAutoRefreshPacket> {

    public static final CustomPacketPayload.Type<NbtExtractorUpdateAutoRefreshPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "nbt_extractor_update_auto_refresh"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NbtExtractorUpdateAutoRefreshPacket> CODEC = getCodec(NbtExtractorUpdateAutoRefreshPacket::new);

    @CodecField
    private BlockPos blockPos;
    @CodecField
    private boolean autoRefresh;

    public NbtExtractorUpdateAutoRefreshPacket() {
        super(TYPE);
    }

    public NbtExtractorUpdateAutoRefreshPacket(BlockPos blockPos, boolean autoRefresh) {
        super(TYPE);
        this.blockPos = blockPos;
        this.autoRefresh = autoRefresh;
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
        IModHelpers.get().getBlockEntityHelpers().get(world, blockPos, BlockEntityNbtExtractor.class)
                .ifPresent(blockEntity -> blockEntity.updateAutoRefresh(autoRefresh));
    }

}
