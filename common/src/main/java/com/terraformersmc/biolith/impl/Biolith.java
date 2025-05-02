package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.commands.BiolithCommands;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.config.BiolithConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Biolith {
    public static final String MOD_ID = "biolith";
    public static final String MOD_NAME = "Biolith";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final BiolithConfigManager CONFIG_MANAGER = new BiolithConfigManager();

    public static void init() {
        // Load the general config if it hasn't been loaded already
        CONFIG_MANAGER.getGeneralConfig();

        BiolithCompat.init();
        BiolithCommands.init();
    }

    public static BiolithConfigManager getConfigManager() {
        return CONFIG_MANAGER;
    }
}
