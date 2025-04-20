package com.terraformersmc.biolith.impl;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Biolith.MOD_ID)
public class BiolithInit {
    public BiolithInit() {
        Biolith.LOGGER.info("Biolith for Forge is initializing...");

        // Watch for server events so we can maintain our status data.
        MinecraftForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> BiomeCoordinator.handleServerStarting(event.getServer()));
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> BiomeCoordinator.handleServerStopped(event.getServer()));

        // Call loader-agnostic init.
        Biolith.init();
    }
}