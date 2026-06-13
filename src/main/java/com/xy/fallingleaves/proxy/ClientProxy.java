package com.xy.fallingleaves.proxy;

import com.xy.fallingleaves.init.ClientMod;
import com.xy.fallingleaves.init.EventHandler;

/**
 * Client-side proxy. Initializes textures, leaf types, and event listeners
 * during Forge pre-initialization.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        ClientMod.init();
        ClientMod.registerEventBus();
        new EventHandler();
    }
}
