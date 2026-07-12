package org.cyclops.integratednbt.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.Reference;
import org.cyclops.integratednbt.client.gui.container.ContainerScreenNbtExtractor;
import org.cyclops.integratednbt.evaluate.NbtExtractorOutputMode;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;

/**
 * Packet for updating a live crafting plan gui.
 * @author rubensworks
 */
public class UpdateClientNbtExtractorPacket extends PacketCodec<UpdateClientNbtExtractorPacket> {

    public static final CustomPacketPayload.Type<UpdateClientNbtExtractorPacket> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(Reference.MOD_ID, "update_client_nbt_extractor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateClientNbtExtractorPacket> CODEC = getCodec(UpdateClientNbtExtractorPacket::new);

    private static ByteMaskMaker maskMaker = new ByteMaskMaker();
    private static final byte MASK_NBT = maskMaker.nextMask();
    private static final byte MASK_ERROR_CODE = maskMaker.nextMask();
    private static final byte MASK_EXTRACTION_PATH = maskMaker.nextMask();
    private static final byte MASK_OUTPUT_MODE = maskMaker.nextMask();
    private static final byte MASK_ERROR_MESSAGE = maskMaker.nextMask();
    private static final byte MASK_AUTO_REFRESH = maskMaker.nextMask();

    private byte updated = 0;
    private UpdateClientNbtExtractorPacket.ErrorCode errorCode;
    private Tag nbt;
    private SegmentedNbtPath path;
    private NbtExtractorOutputMode outputMode;
    private Component errorMessage;
    private boolean autoRefresh;

    public UpdateClientNbtExtractorPacket() {
        super(TYPE);
    }

    public void updateNBT(Tag nbt) {
        this.nbt = nbt;
        this.updated |= MASK_NBT;
    }

    public void updateErrorCode(UpdateClientNbtExtractorPacket.ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.updated |= MASK_ERROR_CODE;
    }

    public void updateExtractionPath(SegmentedNbtPath path) {
        this.path = path;
        this.updated |= MASK_EXTRACTION_PATH;
    }

    public void updateOutputMode(NbtExtractorOutputMode outputMode) {
        this.outputMode = outputMode;
        this.updated |= MASK_OUTPUT_MODE;
    }

    public void updateErrorMessage(Component errorMessage) {
        this.errorMessage = errorMessage;
        this.updated |= MASK_ERROR_MESSAGE;
    }

    public void updateAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
        this.updated |= MASK_AUTO_REFRESH;
    }

    private boolean isUpdated(byte mask) {
        return (this.updated & mask) > 0;
    }

    public boolean isEmpty() {
        return this.updated == 0;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        super.encode(buf);

        buf.writeByte(this.updated);
        if (this.isUpdated(MASK_NBT)) {
            CompoundTag compound = new CompoundTag();
            if (this.nbt != null) {
                compound.put("nbt", this.nbt);
            }
            buf.writeNbt(compound);
        }
        if (this.isUpdated(MASK_ERROR_CODE)) {
            buf.writeByte(this.errorCode.ordinal());
        }
        if (this.isUpdated(MASK_EXTRACTION_PATH)) {
            buf.writeNbt(this.path.toNBTCompound());
        }
        if (this.isUpdated(MASK_OUTPUT_MODE)) {
            buf.writeByte(this.outputMode.ordinal());
        }
        if (this.isUpdated(MASK_ERROR_MESSAGE)) {
            if (this.errorMessage == null) {
                buf.writeBoolean(true);
            } else {
                buf.writeBoolean(false);
                ComponentSerialization.STREAM_CODEC.encode(buf, this.errorMessage);
            }
        }
        if (this.isUpdated(MASK_AUTO_REFRESH)) {
            buf.writeBoolean(this.autoRefresh);
        }
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        super.decode(buf);

        this.updated = buf.readByte();
        if (this.isUpdated(MASK_NBT)) {
            CompoundTag compound = buf.readNbt();
            assert compound != null;
            this.nbt = compound.get("nbt");
        }
        if (this.isUpdated(MASK_ERROR_CODE)) {
            this.errorCode = UpdateClientNbtExtractorPacket.ErrorCode.values()[buf.readByte()];
        }
        if (this.isUpdated(MASK_EXTRACTION_PATH)) {
            this.path = SegmentedNbtPath.fromNBT(buf.readNbt()).orElse(new SegmentedNbtPath());
        }
        if (this.isUpdated(MASK_OUTPUT_MODE)) {
            this.outputMode = NbtExtractorOutputMode.values()[buf.readByte()];
        }
        if (this.isUpdated(MASK_ERROR_MESSAGE)) {
            if (buf.readBoolean()) {
                this.errorMessage = null;
            } else {
                this.errorMessage = ComponentSerialization.STREAM_CODEC.decode(buf);
            }
        }
        if (this.isUpdated(MASK_AUTO_REFRESH)) {
            this.autoRefresh = buf.readBoolean();
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {
        if (isUpdated(MASK_NBT)) {
            ContainerScreenNbtExtractor.updateNBT(nbt);
        }
        if (isUpdated(MASK_ERROR_CODE)) {
            ContainerScreenNbtExtractor.updateError(errorCode);
        }
        if (isUpdated(MASK_EXTRACTION_PATH)) {
            ContainerScreenNbtExtractor.updateExtractionPath(path);
        }
        if (isUpdated(MASK_OUTPUT_MODE)) {
            ContainerScreenNbtExtractor.updateOutputMode(outputMode);
        }
        if (isUpdated(MASK_ERROR_MESSAGE)) {
            ContainerScreenNbtExtractor.updateErrorMessage(errorMessage);
        }
        if (isUpdated(MASK_AUTO_REFRESH)) {
            ContainerScreenNbtExtractor.updateAutoRefresh(autoRefresh);
        }
    }

    @Override
    public void actionServer(Level world, ServerPlayer player) {
        // Do nothing
    }

    public enum ErrorCode {
        NO_ERROR,
        TYPE_ERROR,
        EVAL_ERROR,
        UNEXPECTED_ERROR,
    }

}
