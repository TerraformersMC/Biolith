package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.sub.BiolithCriteria;
import com.terraformersmc.biolith.impl.commands.BiolithCommands;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.config.BiolithConfigManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import com.terraformersmc.biolith.impl.surface.BiolithMaterialConditions;
import net.minecraft.resources.ResourceKey;
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
        BiolithCriteria.init();
        BiolithMaterialConditions.init();
    }

    public static BiolithConfigManager getConfigManager() {
        return CONFIG_MANAGER;
    }

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
    public static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> resourceKey, String path) {
        return ResourceKey.create(resourceKey, id(path));
    }
}
