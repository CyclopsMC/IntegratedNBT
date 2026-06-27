package org.cyclops.integratednbt.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.NBTPath;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;

/**
 * Sets the extraction path in the NBT Extractor.
 * @author rubensworks
 */
public class NbtExtractorSetExtractionPathPacket extends PacketCodec {

    @CodecField
    private BlockPos blockPos;
    @CodecField
    private int defaultNBTId;
    private NBTPath path;

    public NbtExtractorSetExtractionPathPacket() {

    }

    public NbtExtractorSetExtractionPathPacket(BlockPos blockPos, NBTPath path, int defaultNBTId) {
        this.blockPos = blockPos;
        this.path = path;
        this.defaultNBTId = defaultNBTId;
    }

    @Override
    public void encode(FriendlyByteBuf output) {
        super.encode(output);
        output.writeNbt(this.path.toNBTCompound());
    }

    @Override
    public void decode(FriendlyByteBuf input) {
        super.decode(input);
        this.path = NBTPath.fromNBT(input.readNbt()).orElse(new NBTPath());
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
        BlockEntityHelpers.get(world, blockPos, BlockEntityNbtExtractor.class)
                .ifPresent(blockEntity -> {
                    blockEntity.setExtractionPath(path);
                    blockEntity.setDefaultNBTId((byte) defaultNBTId);
                });
    }

}
