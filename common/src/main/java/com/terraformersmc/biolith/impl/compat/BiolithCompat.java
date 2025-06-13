package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.platform.Services;

public class BiolithCompat {
    public static final boolean COMPAT_DATAGEN = System.getProperty("fabric-api.datagen") != null;
    public static final boolean COMPAT_MODERNER_BETA = Services.PLATFORM.isModLoaded("moderner_beta");
    public static final boolean COMPAT_TERRABLENDER = Services.PLATFORM.isModLoaded("terrablender");

    public static void init() {
        if (COMPAT_MODERNER_BETA) {
            Biolith.LOGGER.info("Enabling Biolith's Moderner Beta compatibility layer.");
        }
        if (COMPAT_TERRABLENDER) {
            Biolith.LOGGER.info("Enabling Biolith's TerraBlender compatibility layer.");
        }
    }
}
