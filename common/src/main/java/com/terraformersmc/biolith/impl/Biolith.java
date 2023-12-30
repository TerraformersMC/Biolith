package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.platform.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Biolith {
    public static final String MOD_ID = "biolith";
    public static final String MOD_NAME = "Biolith";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final boolean COMPAT_DATAGEN = System.getProperty("fabric-api.datagen") != null;
    public static final boolean COMPAT_TERRABLENDER = Services.PLATFORM.isModLoaded("terrablender");

    public static void init() {
        if (COMPAT_TERRABLENDER) {
            LOGGER.info("Enabling Biolith's TerraBlender compatibility layer.");
        }
    }
}
