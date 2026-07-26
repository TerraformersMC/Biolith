package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.data.BiomePlacementLoader;
import com.terraformersmc.biolith.impl.data.SurfaceGenerationLoader;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.resource.VanillaServerListeners;

@Mod(Biolith.MOD_ID)
public class BiolithInit {
    public BiolithInit() {
        Biolith.LOGGER.info("Biolith for NeoForge is initializing...");

        // Watch for server events so we can maintain our status data.
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, (ServerAboutToStartEvent event) -> BiomeCoordinator.handleServerStarting(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> BiomeCoordinator.handleServerStopped(event.getServer()));

        // Implement our resource reloaders The Neoforged Way (tm).
        Identifier biomePlacementLoaderId = Biolith.id("biome_placement_loader");
        Identifier surfaceGenerationLoaderId = Biolith.id("surface_generation_loader");
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> {
            event.addListener(biomePlacementLoaderId, new BiomePlacementLoader());
            event.addListener(surfaceGenerationLoaderId, new SurfaceGenerationLoader(event.getServerResources().getRegistryLookup()));
            event.addDependency(VanillaServerListeners.LAST, biomePlacementLoaderId);
            event.addDependency(VanillaServerListeners.LAST, surfaceGenerationLoaderId);
        });

        // Call loader-agnostic init.
        Biolith.init();
    }
}