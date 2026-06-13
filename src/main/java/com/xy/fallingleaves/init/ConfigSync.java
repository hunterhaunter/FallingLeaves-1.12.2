package com.xy.fallingleaves.init;

import com.xy.fallingleaves.FallingLeavesMod;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Synchronizes the {@code @Config} annotation changes when the user edits
 * values through the in-game GUI.
 */
@Mod.EventBusSubscriber(modid = FallingLeavesMod.MOD_ID)
public class ConfigSync {

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (FallingLeavesMod.MOD_ID.equals(event.getModID()))
            ConfigManager.sync(FallingLeavesMod.MOD_ID, Config.Type.INSTANCE);
    }
}
