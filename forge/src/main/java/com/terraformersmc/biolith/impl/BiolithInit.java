package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.data.BiomePlacementLoader;
import com.terraformersmc.biolith.impl.data.SurfaceGenerationLoader;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Biolith.MOD_ID)
public class BiolithInit {
    public BiolithInit() {
        Biolith.LOGGER.info("Biolith for Forge is initializing...");

        // Watch for server events so we can maintain our status data.
        ServerAboutToStartEvent.BUS.addListener(event -> BiomeCoordinator.handleServerStarting(event.getServer()));
        ServerStoppedEvent.BUS.addListener(event -> BiomeCoordinator.handleServerStopped(event.getServer()));

        // Implement our resource reloaders The Forge Way (tm).
        AddReloadListenerEvent.BUS.addListener(event -> event.addListener(new BiomePlacementLoader()));
        AddReloadListenerEvent.BUS.addListener(event -> event.addListener(new SurfaceGenerationLoader()));

        // Call loader-agnostic init.
        Biolith.init();
    }
}