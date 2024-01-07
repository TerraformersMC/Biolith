package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.platform.Services;

public class BiolithCompat {
    public static final boolean COMPAT_DATAGEN = System.getProperty("fabric-api.datagen") != null;
    public static final boolean COMPAT_TERRABLENDER = Services.PLATFORM.isModLoaded("terrablender");

    public static void init() {
        if (COMPAT_TERRABLENDER) {
            Biolith.LOGGER.info("Enabling Biolith's TerraBlender compatibility layer.");
        }
    }
}
