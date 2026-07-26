package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.data.BiomePlacementLoader;
import com.terraformersmc.biolith.impl.data.SurfaceGenerationLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.resources.Identifier;

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

            Identifier biomePlacementLoaderId = Biolith.id("biome_placement_loader");
            Identifier surfaceGenerationLoaderId = Biolith.id("surface_generation_loader");

            // Implement our resource reloaders The Fabric Way (tm).
            DataResourceLoader serverDataLoader = DataResourceLoader.get();
            serverDataLoader.registerReloadListener(biomePlacementLoaderId, new BiomePlacementLoader());
            serverDataLoader.registerReloadListener(surfaceGenerationLoaderId, SurfaceGenerationLoader::new);
            serverDataLoader.addListenerOrdering(ResourceReloaderKeys.AFTER_VANILLA, biomePlacementLoaderId);
            serverDataLoader.addListenerOrdering(ResourceReloaderKeys.AFTER_VANILLA, surfaceGenerationLoaderId);

            // Call loader-agnostic init.
            Biolith.init();
        }
    }
}
