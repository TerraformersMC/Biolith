package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@Mod(Biolith.MOD_ID)
public class BiolithInit {
    public BiolithInit() {
        Biolith.LOGGER.info("Biolith for NeoForge is initializing...");

        // Watch for server events so we can maintain our status data.
        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> BiomeCoordinator.handleServerStarting(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> BiomeCoordinator.handleServerStopped(event.getServer()));

        // Call loader-agnostic init.
        Biolith.init();
    }
}