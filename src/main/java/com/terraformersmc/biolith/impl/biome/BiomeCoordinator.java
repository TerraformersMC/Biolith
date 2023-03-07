package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.config.BiolithState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;

public class BiomeCoordinator {
    public static final DimensionBiomePlacement NETHER = new NetherBiomePlacement();
    public static final DimensionBiomePlacement OVERWORLD = new OverworldBiomePlacement();

    private static BiolithState END_STATE;
    private static BiolithState NETHER_STATE;
    private static BiolithState OVERWORLD_STATE;

    private static boolean serverStarted = false;
    protected static DynamicRegistryManager.Immutable registryManager;

    public static void handleServerStarting(MinecraftServer server) {
        registryManager = server.getCombinedDynamicRegistries().getCombinedRegistryManager();

        if (serverStarted) {
            Biolith.LOGGER.warn("Received notification of server start-up but it should already be running!  O.o");
        } else {
            serverStarted = true;
        }
    }

    public static void handleWorldStarting(ServerWorld world) {
        if (!serverStarted) {
            Biolith.LOGGER.error("New world '{}' created when server is not running!", world.getRegistryKey().getValue());
        }

        if (DimensionTypes.THE_END.equals(world.getDimensionKey())) {
            // TODO: Implement support for The End? (Hint: it's not MultiNoise ... yet)
            END_STATE = new BiolithState(world, "end");
        } else if (DimensionTypes.THE_NETHER.equals(world.getDimensionKey())) {
            NETHER_STATE = new BiolithState(world, "nether");
            NETHER.serverReplaced(NETHER_STATE, world.getSeed());
        } else if (DimensionTypes.OVERWORLD.equals(world.getDimensionKey())) {
            OVERWORLD_STATE = new BiolithState(world, "overworld");
            OVERWORLD.serverReplaced(OVERWORLD_STATE, world.getSeed());
        } else {
            Biolith.LOGGER.info("Ignoring world '{}'; unknown dimension type: {}", world.getRegistryKey().getValue(), world.getDimensionKey().getValue());
        }
    }

    public static void handleServerStopped(MinecraftServer server) {
        serverStarted = false;
        registryManager = null;
        END_STATE = null;
        NETHER_STATE = null;
        OVERWORLD_STATE = null;
    }

    public static @Nullable DynamicRegistryManager.Immutable getRegistryManager() {
        return registryManager;
    }
}
