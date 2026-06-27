package org.cyclops.integratednbt.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;
import org.cyclops.integratednbt.Additions;
import org.cyclops.integratednbt.CabledHorizontalBlock;
import org.cyclops.integratednbt.NBTExtractorBE;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.cyclops.integratednbt.Additions.NBT_EXTRACTOR_BE;

public class BlockNbtExtractor extends CabledHorizontalBlock implements EntityBlock { // TODO: extend BlockEntityActiveVariableBase, like BlockEntityProxy

    public BlockNbtExtractor(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == NBT_EXTRACTOR_BE.get() ? NBTExtractorBE::tick : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public void onRemove(
        BlockState oldState,
        @Nonnull Level worldIn,
        @Nonnull BlockPos pos,
        BlockState newState,
        boolean isMoving
    ) {
        if (oldState.getBlock() != newState.getBlock()) {
            BlockEntity tileEntity = worldIn.getBlockEntity(pos);
            if (tileEntity instanceof NBTExtractorBE) {
                Container inventory = (NBTExtractorBE) tileEntity;
                for (int slot = 0; slot < inventory.getContainerSize(); ++slot) {
                    Containers.dropItemStack(
                        worldIn,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        inventory.getItem(slot)
                    );
                }
            }
        }
        super.onRemove(oldState, worldIn, pos, newState, isMoving);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public InteractionResult use(
        BlockState blockState,
        Level world,
        BlockPos blockPos,
        Player player,
        InteractionHand hand,
        BlockHitResult rayTraceResult
    ) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!world.isClientSide) {
            ServerPlayer playerMP = (ServerPlayer) player;
            if (WrenchHelpers.isWrench(player, heldItem, world, blockPos, rayTraceResult.getDirection())
                && player.isCrouching()
            ) {
                Block.dropResources(
                    blockState,
                    world,
                    blockPos,
                    world.getBlockEntity(blockPos),
                    player,
                    heldItem
                );
                world.destroyBlock(blockPos, false);
                return InteractionResult.SUCCESS;
            }
            if (heldItem.getItem() == Additions.NBT_EXTRACTOR_REMOTE.get()) {
                return InteractionResult.PASS;
            }
            if (!player.isCrouching()) {
                this.playerAccess(world, blockPos, playerMP);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    public void playerAccess(Level world, BlockPos pos, ServerPlayer playerMP) {
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof NBTExtractorBE) {
            NBTExtractorBE nbtExtractorTileEntity = (NBTExtractorBE) tileentity;
            nbtExtractorTileEntity.refreshVariables(true);
            NetworkHooks.openScreen(playerMP, nbtExtractorTileEntity, pos);
        }
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(
        BlockPos blockPos, BlockState blockState
    ) {
        return new NBTExtractorBE(blockPos, blockState);
    }
}
