package org.cyclops.integratednbt.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;
import org.cyclops.integratednbt.NBTExtractorOutputMode;

/**
 * Sets the extraction path in the NBT Extractor.
 * @author rubensworks
 */
public class NbtExtractorSetOutputModePacket extends PacketCodec {

    @CodecField
    private BlockPos blockPos;
    private NBTExtractorOutputMode outputMode;

    public NbtExtractorSetOutputModePacket() {

    }

    public NbtExtractorSetOutputModePacket(BlockPos blockPos, NBTExtractorOutputMode outputMode) {
        this.blockPos = blockPos;
        this.outputMode = outputMode;
    }

    @Override
    public void encode(FriendlyByteBuf output) {
        super.encode(output);
        output.writeByte(this.outputMode.ordinal());
    }

    @Override
    public void decode(FriendlyByteBuf input) {
        super.decode(input);
        this.outputMode = NBTExtractorOutputMode.values()[input.readByte()];
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
                .ifPresent(blockEntity -> blockEntity.setOutputMode(outputMode));
    }

}
