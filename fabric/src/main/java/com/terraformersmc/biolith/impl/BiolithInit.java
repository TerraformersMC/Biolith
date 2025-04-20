package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class BiolithInit implements ModInitializer {
    @Override
    public void onInitialize() {
        Biolith.LOGGER.info("Biolith for Fabric is initializing...");

        // TODO: Is there a better way to do this?
        if (BiolithCompat.COMPAT_DATAGEN) {
            Biolith.LOGGER.info("Suppressing worldgen during datagen...");
        } else {
            // Watch for server events so we can maintain our status data.
            ServerLifecycleEvents.SERVER_STARTING.register(BiomeCoordinator::handleServerStarting);
            ServerLifecycleEvents.SERVER_STOPPED.register(BiomeCoordinator::handleServerStopped);

            // Call loader-agnostic init.
            Biolith.init();
        }
    }
}