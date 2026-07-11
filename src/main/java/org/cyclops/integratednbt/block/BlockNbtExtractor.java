package org.cyclops.integratednbt.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.integrateddynamics.core.block.BlockWithEntityGuiCabled;
import org.cyclops.integratednbt.RegistryEntries;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;

import javax.annotation.Nullable;

public class BlockNbtExtractor extends BlockWithEntityGuiCabled {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockNbtExtractor(Properties properties) {
        super(properties, BlockEntityNbtExtractor::new);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(BlockNbtExtractor::new);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RegistryEntries.BLOCK_ENTITY_NBT_EXTRACTOR.get(), new BlockEntityNbtExtractor.Ticker<>());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public InteractionResult useWithoutItem(
        BlockState blockState,
        Level world,
        BlockPos blockPos,
        Player player,
        BlockHitResult rayTraceResult
    ) {
        if (!world.isClientSide() && player.getMainHandItem().getItem() == RegistryEntries.ITEM_NBT_EXTRACTOR_REMOTE.get()) {
            return InteractionResult.PASS;
        }
        return super.useWithoutItem(blockState, world, blockPos, player, rayTraceResult);
    }

    @Override
    public void writeExtraGuiData(FriendlyByteBuf packetBuffer, Level world, Player player, BlockPos blockPos, BlockHitResult rayTraceResult) {
        super.writeExtraGuiData(packetBuffer, world, player, blockPos, rayTraceResult);
        packetBuffer.writeBlockPos(blockPos);
    }
}
