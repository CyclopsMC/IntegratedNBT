package org.cyclops.integratednbt.client.model;

import net.minecraft.resources.ResourceLocation;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.client.model.IVariableModelProviderRegistry;
import org.cyclops.integrateddynamics.core.client.model.SingleVariableModelProvider;
import org.cyclops.integratednbt.Reference;

/**
 * Collection of variable model providers.
 * @author rubensworks
 */
public class VariableModelProviders {

    public static final IVariableModelProviderRegistry REGISTRY = IntegratedDynamics._instance.getRegistryManager().getRegistry(IVariableModelProviderRegistry.class);

    public static final SingleVariableModelProvider NBT_EXTRACTED = REGISTRY.addProvider(new SingleVariableModelProvider(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "customoverlay/nbt_extracted")));

    public static void load() {}

}
