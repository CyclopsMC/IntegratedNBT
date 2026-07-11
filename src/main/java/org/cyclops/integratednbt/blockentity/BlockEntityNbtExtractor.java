package org.cyclops.integratednbt.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityActiveVariableBase;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integratednbt.RegistryEntries;
import org.cyclops.integratednbt.evaluate.NbtExtractorOutputMode;
import org.cyclops.integratednbt.evaluate.nbt.path.SegmentedNbtPath;
import org.cyclops.integratednbt.helpers.Wrapper;
import org.cyclops.integratednbt.inventory.container.ContainerNbtExtractor;
import org.cyclops.integratednbt.network.NbtExtractorNetworkElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

public class BlockEntityNbtExtractor extends BlockEntityActiveVariableBase<NbtExtractorNetworkElement> implements MenuProvider {

    public static final int SRC_NBT_SLOT = 0;
    public static final int VAR_OUT_SLOT = 1;
    private VariableContainerDefault variableContainerCapability = new VariableContainerDefault();
    /**
     * A set of expanded paths in this extractor;
     * <p>
     * This is client-side only and it is not persisted.
     */
    private HashSet<SegmentedNbtPath> expandedPaths;
    /**
     * How much has the user scrolled in this extractor;
     * <p>
     * This is client-side only and it is not persisted.
     */
    private Wrapper<Integer> scrollTop = new Wrapper<>(0);
    /**
     * Whether should run refreshVariable on next tick
     */
    private boolean shouldRefreshVariable = false;
    /**
     * Whether should send update on next tick
     */
    private boolean shouldUpdateOutVariable = false;
    private SegmentedNbtPath extractionPath = new SegmentedNbtPath();
    private byte defaultNBTId = 1;
    private NbtExtractorOutputMode outputMode = NbtExtractorOutputMode.REFERENCE;
    private Tag lastEvaluatedNBT = null;
    // If null, then there is no frozen value available
    private Wrapper<Tag> frozenNBT = null;
    private boolean autoRefresh = true;
    /**
     * The item stack that yielded the current frozen NBT
     */
    private ItemStack frozenNBTItemStack = ItemStack.EMPTY;
    private Player lastPlayer;

    public BlockEntityNbtExtractor(BlockPos pos, BlockState state) {
        this(RegistryEntries.BLOCK_ENTITY_NBT_EXTRACTOR, pos, state, 2);
        this.expandedPaths = new HashSet<>();
        this.expandedPaths.add(new SegmentedNbtPath());
    }

    public BlockEntityNbtExtractor(BlockEntityType<?> type, BlockPos blockPos, BlockState blockState, int inventorySize) {
        super(type, blockPos, blockState, inventorySize);

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, LazyOptional.of(() -> new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new NbtExtractorNetworkElement(DimPos.of(world, blockPos));
            }
        }));
        addCapabilityInternal(VariableContainerConfig.CAPABILITY, LazyOptional.of(() -> variableContainerCapability));
    }

    public void setLastPlayer(Player player) {
        this.lastPlayer = player;
    }

    @Override
    public int getSlotRead() {
        return SRC_NBT_SLOT;
    }

    public void setShouldRefreshVariable(boolean shouldRefreshVariable) {
        this.shouldRefreshVariable = shouldRefreshVariable;
    }

    public boolean isShouldRefreshVariable() {
        return shouldRefreshVariable;
    }

    public void setShouldUpdateOutVariable(boolean shouldUpdateOutVariable) {
        this.shouldUpdateOutVariable = shouldUpdateOutVariable;
    }

    public boolean isShouldUpdateOutVariable() {
        return shouldUpdateOutVariable;
    }

    public NbtExtractorOutputMode getOutputMode() {
        return this.outputMode;
    }

    public void setOutputMode(NbtExtractorOutputMode outputMode) {
        this.outputMode = outputMode;
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level == null) {
            return;
        }
        if (!this.level.isClientSide) {
            this.shouldUpdateOutVariable = true;
            if (!this.autoRefresh &&
                    !ItemStack.matches(this.getInventory().getItem(SRC_NBT_SLOT), this.frozenNBTItemStack)) {
                this.frozenNBTItemStack = this.getInventory().getItem(SRC_NBT_SLOT);
                this.frozenNBT = null;
            }
        }
    }

    @Override
    protected void updateReadVariable(boolean sendVariablesUpdateEvent) {
        super.updateReadVariable(sendVariablesUpdateEvent);
        this.variableContainerCapability.refreshVariables(
                getNetwork(),
                getInventory(),
                sendVariablesUpdateEvent,
                ValueDeseralizationContext.of(this.level)
        );
    }

    public void setDefaultNBTId(byte defaultNBTId) {
        if (defaultNBTId < 1 || defaultNBTId > 12) {
            this.defaultNBTId = 1;
        } else {
            this.defaultNBTId = defaultNBTId;
        }
        this.setChanged();
    }

    public SegmentedNbtPath getExtractionPath() {
        return this.extractionPath;
    }

    public void setExtractionPath(SegmentedNbtPath extractionPath) {
        this.extractionPath = extractionPath;
        this.setChanged();
    }

    public boolean isAutoRefresh() {
        return this.autoRefresh;
    }

    public void updateAutoRefresh(boolean autoRefresh) {
        if (this.autoRefresh == autoRefresh) {
            return;
        }
        this.autoRefresh = autoRefresh;
        if (!autoRefresh) {
            this.frozenNBT = null;
            this.frozenNBTItemStack = ItemStack.EMPTY;
        }
        this.setChanged();
    }

    public void updateLastEvaluatedNBT(Tag lastEvaluatedNBT) {
        this.lastEvaluatedNBT = lastEvaluatedNBT;
        if (!this.autoRefresh && this.frozenNBT == null) {
            this.frozenNBT = Wrapper.of(this.lastEvaluatedNBT);
            this.frozenNBTItemStack = this.getInventory().getItem(SRC_NBT_SLOT).copy();
        }
    }

    public HashSet<SegmentedNbtPath> getExpandedPaths() {
        return this.expandedPaths;
    }

    public Wrapper<Integer> getScrollTop() {
        return this.scrollTop;
    }

    public IVariable<?> getSrcNBTVariable() {
        INetwork network = this.getNetwork();
        IPartNetwork partNetwork = NetworkHelpers.getPartNetwork(network).orElse(null);
        if (partNetwork == null) {
            return null;
        }
        return getEvaluator().getVariable(network, partNetwork);
    }

    public Component getFirstErrorMessage() {
        List<MutableComponent> errors = getEvaluator().getErrors();
        if (errors.isEmpty()) {
            return null;
        } else {
            return errors.get(0);
        }
    }

    public Wrapper<Tag> getFrozenValue() {
        if (this.autoRefresh) {
            return null;
        } else {
            return this.frozenNBT;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!MinecraftHelpers.isClientSide()) {
            this.shouldRefreshVariable = true;
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag errorsList = new ListTag();
        tag.put("path", this.extractionPath.toNBT());
        tag.putByte("defaultNBTId", this.defaultNBTId);
        tag.putByte("outputMode", (byte) this.outputMode.ordinal());
        tag.putBoolean("isAutoRefresh", this.autoRefresh);
        if (!this.autoRefresh) {
            // frozenNBT = null:
            // { ... }
            //
            // frozenNBT = Wrapper.of(null):
            // { ..., frozenNBT: {} }
            //
            // frozenNBT = Wrapper.of(something):
            // {..., frozenNBT: { value: something } }

            if (this.frozenNBT != null) {
                CompoundTag compound = new CompoundTag();
                if (this.frozenNBT.get() != null) {
                    compound.put("value", this.frozenNBT.get());
                }
                tag.put("frozenNBT", compound);
            }
            tag.put(
                "frozenNBTItemStack",
                this.frozenNBTItemStack.save(new CompoundTag())
            );
        }
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return Component.translatable("tile.integratednbt:nbt_extractor.name");
    }

    @Override
    public void read(CompoundTag tag) {
        super.read(tag);
        if (tag.contains("path")) {
            this.extractionPath = SegmentedNbtPath.fromNBT(tag.get("path")).orElse(new SegmentedNbtPath());
        }
        if (tag.contains("defaultNBTId")) {
            this.defaultNBTId = tag.getByte("defaultNBTId");
        }
        if (tag.contains("outputMode")) {
            this.outputMode = NbtExtractorOutputMode.values()[tag.getByte("outputMode")];
        }
        if (tag.contains("isAutoRefresh")) {
            this.autoRefresh = tag.getBoolean("isAutoRefresh");
            if (!this.autoRefresh) {
                if (tag.contains("frozenNBT")) {
                    this.frozenNBT = Wrapper.of(tag.getCompound("frozenNBT").get("value"));
                }
                this.frozenNBTItemStack = ItemStack.of(tag.getCompound("frozenNBTItemStack"));
            }
        }
        this.shouldRefreshVariable = true;
    }

    public void afterNetworkReAlive() {
        this.shouldRefreshVariable = true;
    }

    public void refreshVariables(boolean sendVariablesUpdateEvent) {
        updateReadVariable(sendVariablesUpdateEvent);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerNbtExtractor(windowId, inventory, this);
    }

    public void updateOutVariable() {
        if (!this.getInventory().getItem(VAR_OUT_SLOT).isEmpty()) {
            ItemStack result = this.outputMode.writeItemStack(
                () -> {
                    this.updateReadVariable(true);
                    return getEvaluator().getVariableFacade();
                },
                this.getInventory().getItem(VAR_OUT_SLOT),
                (!this.autoRefresh && this.frozenNBT != null)
                    ? this.frozenNBT.get()
                    : this.lastEvaluatedNBT,
                this.extractionPath,
                this.defaultNBTId,
                this.getLevel(),
                this.getBlockState(),
                this.lastPlayer
            );
            if (result != null) {
                this.getInventory().setItem(VAR_OUT_SLOT, result);
            }
        }
    }

    public static class Ticker<T extends BlockEntityNbtExtractor> extends BlockEntityCableConnectableInventory.Ticker<T> {
        @Override
        protected void update(Level level, BlockPos pos, BlockState blockState, T blockEntity) {
            super.update(level, pos, blockState, blockEntity);

            if (!level.isClientSide) {
                if (blockEntity.isShouldRefreshVariable() && blockEntity.getNetwork() != null) {
                    blockEntity.setShouldRefreshVariable(false);
                    blockEntity.refreshVariables(true);
                }
                if (blockEntity.isShouldUpdateOutVariable()) {
                    blockEntity.updateOutVariable();
                    blockEntity.setShouldUpdateOutVariable(false);
                }
            }
        }
    }
}
