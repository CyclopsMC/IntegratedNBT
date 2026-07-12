package org.cyclops.integratednbt.evaluate.variable;

import net.minecraft.client.renderer.item.ItemModel;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelBaked;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeClient;
import org.cyclops.integratednbt.client.model.VariableModelProviders;

/**
 * Client-side component for NbtExtractedVariableFacade that handles model overlays.
 * @author rubensworks
 */
public class NbtExtractedVariableFacadeClient implements IVariableFacadeClient {

    private final NbtExtractedVariableFacade variableFacade;

    public NbtExtractedVariableFacadeClient(NbtExtractedVariableFacade variableFacade) {
        this.variableFacade = variableFacade;
    }

    @Override
    public ItemModel getItemModelOverlay(IVariableModelBaked variableModelBaked) {
        if (variableFacade.isValid()) {
            return variableModelBaked.getSubModels(VariableModelProviders.NBT_EXTRACTED).getBakedModel();
        }
        return null;
    }

}
