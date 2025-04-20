package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Biolith {
    public static final String MOD_ID = "biolith";
    public static final String MOD_NAME = "Biolith";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        // Load the general config if it hasn't been loaded already
        //CONFIG_MANAGER.getGeneralConfig();

        BiolithCompat.init();
        //BiolithCommands.init();
    }
}
