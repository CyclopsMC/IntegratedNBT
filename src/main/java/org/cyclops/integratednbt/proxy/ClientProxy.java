package org.cyclops.integratednbt.proxy;

import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;
import org.cyclops.integratednbt.IntegratedNbt;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxy extends ClientProxyComponent {

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBaseNeoForge getMod() {
        return IntegratedNbt._instance;
    }

}
