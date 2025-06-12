package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.biolith.impl.platform.Services;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
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

    public static boolean isServerStarted() {
        return serverStarted;
    }

    public static void setRegistryManager(CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries) {
        // Called by biolith$earlyCaptureRegistries() in MixinMinecraftServer and MixinServerLoader so we can set this really early.
        registryManager = combinedDynamicRegistries.getCombinedRegistryManager();
    }

    public static @Nullable DynamicRegistryManager.Immutable getRegistryManager() {
        return registryManager;
    }

    public static Optional<Registry<Biome>> getBiomeLookup() {
        if (registryManager == null) {
            return Optional.empty();
        }

        return registryManager.getOptional(RegistryKeys.BIOME);
    }

    public static RegistryEntryLookup<Biome> getBiomeLookupOrThrow() {
        return getBiomeLookup().orElseThrow();
    }

    public static void handleServerStarting(MinecraftServer server) {
        if (registryManager == null) {
            registryManager = server.getCombinedDynamicRegistries().getCombinedRegistryManager();
        }

        // When TerraBlender is present, it ignores our surface rules.
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
        Optional<RegistryKey<DimensionType>> dimensionKey = world.getDimensionEntry().getKey();

        if (!serverStarted) {
            Biolith.LOGGER.error("New world '{}' created when server is not running!", world.getRegistryKey().getValue());
        }

        if (dimensionKey.isPresent()) {
            if (DimensionTypes.THE_END.equals(dimensionKey.get())) {
                if (END_STATE == null) {
                    END_STATE = world.getPersistentStateManager().getOrCreate(BiolithState.getPersistentStateType("end"));
                    END.serverReplaced(END_STATE, world);
                } else {
                    Biolith.LOGGER.warn("More than one End dimension world created; cowardly ignoring '{}' in favor of '{}'", world.getRegistryKey().getValue(), END_STATE.getWorldId());
                }
            } else if (DimensionTypes.THE_NETHER.equals(dimensionKey.get())) {
                if (NETHER_STATE == null) {
                    NETHER_STATE = world.getPersistentStateManager().getOrCreate(BiolithState.getPersistentStateType("nether"));
                    NETHER.serverReplaced(NETHER_STATE, world);
                } else {
                    Biolith.LOGGER.warn("More than one Nether dimension world created; cowardly ignoring '{}' in favor of '{}'", world.getRegistryKey().getValue(), NETHER_STATE.getWorldId());
                }
            } else if (DimensionTypes.OVERWORLD.equals(dimensionKey.get())) {
                if (OVERWORLD_STATE == null) {
                    OVERWORLD_STATE = world.getPersistentStateManager().getOrCreate(BiolithState.getPersistentStateType("overworld"));
                    OVERWORLD.serverReplaced(OVERWORLD_STATE, world);
                } else {
                    Biolith.LOGGER.warn("More than one Overworld dimension world created; cowardly ignoring '{}' in favor of '{}'", world.getRegistryKey().getValue(), OVERWORLD_STATE.getWorldId());
                }
            } else {
                Biolith.LOGGER.info("Ignoring world '{}'; unknown dimension type: {}", world.getRegistryKey().getValue(), dimensionKey.get().getValue());
            }
        } else {
            Biolith.LOGGER.info("Ignoring world '{}'; world has no associated dimension", world.getRegistryKey().getValue());
        }
    }

    public static void handleServerStopped(MinecraftServer server) {
        serverStarted = false;
        registryManager = null;

        END_STATE = null;
        NETHER_STATE = null;
        OVERWORLD_STATE = null;

        END.serverStopped();
        NETHER.serverStopped();
        OVERWORLD.serverStopped();
    }
}
