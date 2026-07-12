package org.cyclops.integratednbt.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component value for storing the binding of an NBT extractor remote item to a block position.
 * @author rubensworks
 */
public record NbtExtractorRemoteBoundData(String dimensionId, BlockPos pos) {

    public static final Codec<NbtExtractorRemoteBoundData> CODEC = RecordCodecBuilder.create(rb ->
            rb.group(
                    Codec.STRING.fieldOf("dimension").forGetter(NbtExtractorRemoteBoundData::dimensionId),
                    BlockPos.CODEC.fieldOf("pos").forGetter(NbtExtractorRemoteBoundData::pos)
            ).apply(rb, NbtExtractorRemoteBoundData::new)
    );

    public static final StreamCodec<io.netty.buffer.ByteBuf, NbtExtractorRemoteBoundData> STREAM_CODEC =
            StreamCodec.composite(
                    net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, NbtExtractorRemoteBoundData::dimensionId,
                    BlockPos.STREAM_CODEC, NbtExtractorRemoteBoundData::pos,
                    NbtExtractorRemoteBoundData::new
            );
}
