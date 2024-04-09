package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.biolith.impl.platform.Services;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BiomeCoordinator {
    public static final EndBiomePlacement END = new EndBiomePlacement();
    public static final NetherBiomePlacement NETHER = new NetherBiomePlacement();
    public static final OverworldBiomePlacement OVERWORLD = new OverworldBiomePlacement();
    private static boolean registeredWithTerrablender = false;

    private static BiolithState END_STATE;
    private static BiolithState NETHER_STATE;
    private static BiolithState OVERWORLD_STATE;

    private static boolean serverStarted = false;
    protected static DynamicRegistryManager.Immutable registryManager;

    public static void setRegistryManager(CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries) {
        // Called by biolith$earlyCaptureRegistries() in MixinMinecraftServer and MixinServerLoader so we can set this really early.
        registryManager = combinedDynamicRegistries.getCombinedRegistryManager();
    }

    public static @Nullable DynamicRegistryManager.Immutable getRegistryManager() {
        return registryManager;
    }

    public static Optional<RegistryWrapper.Impl<Biome>> getBiomeLookup() {
        DynamicRegistryManager.Immutable registryManager = BiomeCoordinator.getRegistryManager();
        if (registryManager == null) {
            return Optional.empty();
        }

        return registryManager.getOptionalWrapper(RegistryKeys.BIOME);
    }

    public static RegistryEntryLookup<Biome> getBiomeLookupOrThrow() {
        return getBiomeLookup().orElseThrow();
    }

    public static void handleServerStarting(MinecraftServer server) {
        if (registryManager == null) {
            registryManager = server.getCombinedDynamicRegistries().getCombinedRegistryManager();
        }

        // When TerraBlender is present, it ignores our surface rules in the Overworld and Nether.
        // To avoid this, we submit a duplicate registration to TerraBlender (but only once).
        if (BiolithCompat.COMPAT_TERRABLENDER && !registeredWithTerrablender) {
            Services.PLATFORM.getTerraBlenderCompat().registerSurfaceRules();
            registeredWithTerrablender = true;
        }

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
            END_STATE = new BiolithState(world, "end");
            END.serverReplaced(END_STATE, world.getSeed());
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
}