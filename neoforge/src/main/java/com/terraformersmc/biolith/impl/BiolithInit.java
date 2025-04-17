package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.data.BiomePlacementLoader;
import com.terraformersmc.biolith.impl.data.SurfaceGenerationLoader;
import net.minecraft.util.Identifier;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@Mod(Biolith.MOD_ID)
public class BiolithInit {
    public BiolithInit() {
        Biolith.LOGGER.info("Biolith for NeoForge is initializing...");

        // Watch for server events so we can maintain our status data.
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> BiomeCoordinator.handleServerStarting(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> BiomeCoordinator.handleServerStopped(event.getServer()));

        // Implement our resource reloaders The Neoforged Way (tm).
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> event.addListener(Identifier.of(Biolith.MOD_ID, "biome_placement_loader"), new BiomePlacementLoader()));
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> event.addListener(Identifier.of(Biolith.MOD_ID, "surface_generation_loader"), new SurfaceGenerationLoader()));

        // Call loader-agnostic init.
        Biolith.init();
    }
}