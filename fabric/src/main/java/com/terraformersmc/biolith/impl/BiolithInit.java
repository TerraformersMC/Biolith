package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.data.BiomePlacementLoader;
import com.terraformersmc.biolith.impl.data.IdentifiableResourceReloaderWrapper;
import com.terraformersmc.biolith.impl.data.SurfaceGenerationLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

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

            // Implement our resource reloaders The Fabric Way (tm).
            ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloaderWrapper("biome_placement_loader", new BiomePlacementLoader()));
            ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloaderWrapper("surface_generation_loader", new SurfaceGenerationLoader()));

            // Call loader-agnostic init.
            Biolith.init();
        }
    }
}
