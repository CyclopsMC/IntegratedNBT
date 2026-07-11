package org.cyclops.integratednbt.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integratednbt.IntegratedNbt;
import org.cyclops.integratednbt.Reference;
import org.cyclops.integratednbt.RegistryEntries;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;
import org.cyclops.integratednbt.network.packet.OpenNbtExtractorRemoteGuiPacket;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemNbtExtractorRemote extends Item {

    public ItemNbtExtractorRemote(Item.Properties properties) {
        super(properties);
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(
        Level world,
        Player player,
        @Nonnull InteractionHand hand
    ) {
        if (world.isClientSide) {
            this.clientUse(player.getItemInHand(hand), player);
        }
        return super.use(world, player, hand);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientUse(ItemStack itemStack, Player player) {
        CompoundTag nbt = this.getModNBT(itemStack);
        if (!nbt.contains("world")) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.need_bind"));
            return;
        }
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        if (!world.dimension().location().toString().equals(nbt.getString("world"))) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.require_dim"));
            return;
        }
        BlockPos pos = new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
        if (!world.isLoaded(pos)) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.require_load_client"));
            return;
        }
        if (world.getBlockState(pos).getBlock() != RegistryEntries.BLOCK_NBT_EXTRACTOR.get()) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        IntegratedNbt._instance.getPacketHandler().sendToServer(new OpenNbtExtractorRemoteGuiPacket());
    }

    public CompoundTag getModNBT(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound(Reference.MOD_ID);
    }

    public void serverUse(ItemStack itemStack, ServerPlayer player) {
        CompoundTag nbt = this.getModNBT(itemStack);
        if (!nbt.contains("world")) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.need_bind"));
            return;
        }
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(nbt.getString("world")));
        MinecraftServer server = player.getServer();
        if (server == null) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        Level world = server.getLevel(dimensionKey);
        if (world == null) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        BlockPos pos = new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
        if (!world.isLoaded(pos)) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.require_load_server"));
            return;
        }
        if (world.getBlockState(pos).getBlock() != RegistryEntries.BLOCK_NBT_EXTRACTOR.get()) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        playerAccess(world, pos, player);
    }

    public void playerAccess(Level level, BlockPos pos, ServerPlayer playerMP) {
        BlockEntityHelpers.get(level, pos, BlockEntityNbtExtractor.class)
                .ifPresent(blockEntity -> {
                    blockEntity.refreshVariables(true);
                    playerMP.openMenu(blockEntity, buf -> buf.writeBlockPos(pos));
                });
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext itemUseContext) {
        Level world = itemUseContext.getLevel();
        BlockPos pos = itemUseContext.getClickedPos();
        Player player = itemUseContext.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        InteractionHand hand = itemUseContext.getHand();
        if (world.getBlockState(pos).getBlock() == RegistryEntries.BLOCK_NBT_EXTRACTOR.get()) {
            if (!world.isClientSide) {
                RegistryEntries.ITEM_NBT_EXTRACTOR_REMOTE.get()
                    .bindBlock(player.getItemInHand(hand), world, pos);
                player.sendSystemMessage(Component.translatable(
                    "integratednbt:nbt_extractor_remote.bind_successful",
                    String.valueOf(pos.getX()),
                    String.valueOf(pos.getY()),
                    String.valueOf(pos.getZ())
                ));
            }
        } else if (world.isClientSide) {
            this.clientUse(player.getItemInHand(hand), player);
        }
        return InteractionResult.SUCCESS;
    }

    public void bindBlock(ItemStack itemStack, Level world, BlockPos pos) {
        itemStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, customData -> {
            CompoundTag tag = customData.copyTag();
            CompoundTag modNbt = tag.getCompound(Reference.MOD_ID);
            modNbt.putString("world", world.dimension().location().toString());
            modNbt.putInt("x", pos.getX());
            modNbt.putInt("y", pos.getY());
            modNbt.putInt("z", pos.getZ());
            tag.put(Reference.MOD_ID, modNbt);
            return CustomData.of(tag);
        });
    }

    @Override
    public void appendHoverText(
        ItemStack itemStack,
        Item.TooltipContext context,
        List<Component> tooltip,
        TooltipFlag flag
    ) {
        super.appendHoverText(itemStack, context, tooltip, flag);
        CompoundTag nbt = this.getModNBT(itemStack);
        if (nbt.contains("world")) {
            tooltip.add(Component.translatable(
                "integratednbt:nbt_extractor_remote.tooltip.bound",
                String.valueOf(nbt.getInt("x")),
                String.valueOf(nbt.getInt("y")),
                String.valueOf(nbt.getInt("z")),
                nbt.getString("world")
            ).withStyle(style -> style.withColor(ChatFormatting.GREEN)));
        } else {
            tooltip.add(Component.translatable(
                "integratednbt:nbt_extractor_remote.tooltip.not_bound"));
        }
        tooltip.add(Component.translatable("integratednbt:nbt_extractor_remote.tooltip"));
    }
}
