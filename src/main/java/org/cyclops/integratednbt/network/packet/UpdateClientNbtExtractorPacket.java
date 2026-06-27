package org.cyclops.integratednbt.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.cyclops.integratednbt.NBTExtractorOutputMode;
import org.cyclops.integratednbt.client.gui.container.ContainerScreenNbtExtractor;
import org.cyclops.integratednbt.NBTPath;

/**
 * Packet for updating a live crafting plan gui.
 * @author rubensworks
 */
public class UpdateClientNbtExtractorPacket extends PacketCodec {

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
    private NBTPath path;
    private NBTExtractorOutputMode outputMode;
    private Component errorMessage;
    private boolean autoRefresh;

    public UpdateClientNbtExtractorPacket() {

    }

    public void updateNBT(Tag nbt) {
        this.nbt = nbt;
        this.updated |= MASK_NBT;
    }

    public void updateErrorCode(UpdateClientNbtExtractorPacket.ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.updated |= MASK_ERROR_CODE;
    }

    public void updateExtractionPath(NBTPath path) {
        this.path = path;
        this.updated |= MASK_EXTRACTION_PATH;
    }

    public void updateOutputMode(NBTExtractorOutputMode outputMode) {
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
    public void encode(FriendlyByteBuf buf) {
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
            if (this.errorMessage == null) { // Is null
                buf.writeBoolean(true);
            } else {
                buf.writeBoolean(false);
                buf.writeComponent(this.errorMessage);
            }
        }
        if (this.isUpdated(MASK_AUTO_REFRESH)) {
            buf.writeBoolean(this.autoRefresh);
        }
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
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
            this.path = NBTPath.fromNBT(buf.readNbt()).orElse(new NBTPath());
        }
        if (this.isUpdated(MASK_OUTPUT_MODE)) {
            this.outputMode = NBTExtractorOutputMode.values()[buf.readByte()];
        }
        if (this.isUpdated(MASK_ERROR_MESSAGE)) {
            if (buf.readBoolean()) { // Is null
                this.errorMessage = null;
            } else {
                this.errorMessage = buf.readComponent();
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
