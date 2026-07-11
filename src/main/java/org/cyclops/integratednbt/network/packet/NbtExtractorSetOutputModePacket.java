package org.cyclops.integratednbt.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.Reference;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;
import org.cyclops.integratednbt.evaluate.NbtExtractorOutputMode;

/**
 * Sets the extraction path in the NBT Extractor.
 * @author rubensworks
 */
public class NbtExtractorSetOutputModePacket extends PacketCodec<NbtExtractorSetOutputModePacket> {

    public static final CustomPacketPayload.Type<NbtExtractorSetOutputModePacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "nbt_extractor_set_output_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NbtExtractorSetOutputModePacket> CODEC = getCodec(NbtExtractorSetOutputModePacket::new);

    @CodecField
    private BlockPos blockPos;
    private NbtExtractorOutputMode outputMode;

    public NbtExtractorSetOutputModePacket() {
        super(TYPE);
    }

    public NbtExtractorSetOutputModePacket(BlockPos blockPos, NbtExtractorOutputMode outputMode) {
        super(TYPE);
        this.blockPos = blockPos;
        this.outputMode = outputMode;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf output) {
        super.encode(output);
        output.writeByte(this.outputMode.ordinal());
    }

    @Override
    public void decode(RegistryFriendlyByteBuf input) {
        super.decode(input);
        this.outputMode = NbtExtractorOutputMode.values()[input.readByte()];
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
