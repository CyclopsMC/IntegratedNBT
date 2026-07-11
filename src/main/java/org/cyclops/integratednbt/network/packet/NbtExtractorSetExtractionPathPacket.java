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
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;

/**
 * Sets the extraction path in the NBT Extractor.
 * @author rubensworks
 */
public class NbtExtractorSetExtractionPathPacket extends PacketCodec<NbtExtractorSetExtractionPathPacket> {

    public static final CustomPacketPayload.Type<NbtExtractorSetExtractionPathPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "nbt_extractor_set_extraction_path"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NbtExtractorSetExtractionPathPacket> CODEC = getCodec(NbtExtractorSetExtractionPathPacket::new);

    @CodecField
    private BlockPos blockPos;
    @CodecField
    private int defaultNBTId;
    private SegmentedNbtPath path;

    public NbtExtractorSetExtractionPathPacket() {
        super(TYPE);
    }

    public NbtExtractorSetExtractionPathPacket(BlockPos blockPos, SegmentedNbtPath path, int defaultNBTId) {
        super(TYPE);
        this.blockPos = blockPos;
        this.path = path;
        this.defaultNBTId = defaultNBTId;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf output) {
        super.encode(output);
        output.writeNbt(this.path.toNBTCompound());
    }

    @Override
    public void decode(RegistryFriendlyByteBuf input) {
        super.decode(input);
        this.path = SegmentedNbtPath.fromNBT(input.readNbt()).orElse(new SegmentedNbtPath());
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
