package org.cyclops.integratednbt.network;

import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.TileNetworkElement;
import org.cyclops.integratednbt.GeneralConfig;
import org.cyclops.integratednbt.blockentity.BlockEntityNbtExtractor;

import java.util.Optional;

/**
 * @author rubensworks
 */
public class NbtExtractorNetworkElement extends TileNetworkElement<BlockEntityNbtExtractor>
    implements IEventListenableNetworkElement<BlockEntityNbtExtractor> {

    public NbtExtractorNetworkElement(DimPos pos) {
        super(pos);
    }

    @Override
    public void setPriorityAndChannel(INetwork network, int priority, int channel) {

    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getChannel() {
        return IPositionedAddonsNetwork.DEFAULT_CHANNEL;
    }

    @Override
    public int getConsumptionRate() {
        return GeneralConfig.nbtExtractorBaseConsumption;
    }

    @Override
    public Optional<BlockEntityNbtExtractor> getNetworkEventListener() {
        return getTile();
    }

    @Override
    protected Class<BlockEntityNbtExtractor> getTileClass() {
        return BlockEntityNbtExtractor.class;
    }

    @Override
    public boolean onNetworkAddition(INetwork network) {
        BlockEntityNbtExtractor blockEntity = getTile().orElse(null);
        if (blockEntity == null || !blockEntity.hasLevel()) {
            return false;
        }
        return NetworkHelpers.getPartNetwork(network).map(partNetwork -> partNetwork
                .addVariableContainer(DimPos.of(
                        blockEntity.getLevel(),
                        blockEntity.worldPosition
                ))
        ).orElse(false);
    }

    @Override
    public void onNetworkRemoval(INetwork network) {
        BlockEntityNbtExtractor blockEntity = getTile().orElse(null);
        if (blockEntity == null || !blockEntity.hasLevel()) {
            return;
        }
        NetworkHelpers.getPartNetwork(network).ifPresent(partNetwork -> partNetwork
                .removeVariableContainer(DimPos.of(
                        blockEntity.getLevel(),
                        blockEntity.worldPosition
                ))
        );
    }
}
