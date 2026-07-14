package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.biolith.impl.platform.Services;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BiomeCoordinator {
    public static final EndBiomePlacement END = new EndBiomePlacement();
    public static final NetherBiomePlacement NETHER = new NetherBiomePlacement();
    public static final OverworldBiomePlacement OVERWORLD = new OverworldBiomePlacement();

    private static @Nullable BiolithState END_STATE;
    private static @Nullable BiolithState NETHER_STATE;
    private static @Nullable BiolithState OVERWORLD_STATE;

    private static boolean serverStarted = false;
    private static boolean registeredWithTerrablender = false;
    private static @Nullable RegistryAccess registryManager;
    private static @Nullable HolderGetter<LevelStem> dimensionLookup;
    private static @Nullable HolderGetter<Biome> biomeLookup;

    public static boolean isServerStarted() {
        return serverStarted;
    }

    public static void setRegistryManager(LayeredRegistryAccess<RegistryLayer> combinedDynamicRegistries) {
        // Called by biolith$earlyCaptureRegistries() in MixinMinecraftServer and MixinServerLoader
        // so we can set this really early.
        registryManager = combinedDynamicRegistries.compositeAccess();
        dimensionLookup = registryManager.lookupOrThrow(Registries.LEVEL_STEM);
        biomeLookup = registryManager.lookupOrThrow(Registries.BIOME);
    }

    public static void setEarlyBiomeLookup(HolderGetter<Biome> earlyBiomeLookup) {
        if (biomeLookup == null) {
            biomeLookup = earlyBiomeLookup;
        }
    }

    public static @Nullable RegistryAccess getRegistryManager() {
        return registryManager;
    }

    public static Optional<? extends HolderGetter<Biome>> getBiomeLookup() {
        if (biomeLookup != null) {
            return Optional.of(biomeLookup);
        }

        if (registryManager != null) {
            return registryManager.lookup(Registries.BIOME);
        }

        return Optional.empty();
    }

    public static HolderGetter<Biome> getBiomeLookupOrThrow() {
        return getBiomeLookup().orElseThrow();
    }

    public static void handleServerStarting(MinecraftServer server) {
        // This is the "right" way to do it, but in practice it should already be set.
        if (registryManager == null) {
            setRegistryManager(server.registries());
        }

        // When TerraBlender is present, it ignores our surface rules.
        // To avoid this, we submit a duplicate registration to TerraBlender (but only once).
        // TODO: Pretty sure this breaks for datapack surface rules added after first startup...
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

    public static void handleWorldStarting(ServerLevel world) {
        Optional<ResourceKey<DimensionType>> dimensionKey = world.dimensionTypeRegistration().unwrapKey();

        if (!serverStarted) {
            Biolith.LOGGER.error("New world '{}' created when server is not running!", world.dimension().identifier());
        }

        if (dimensionKey.isPresent()) {
            if (BuiltinDimensionTypes.END.equals(dimensionKey.get())) {
                if (END_STATE == null) {
                    END_STATE = world.getDataStorage().computeIfAbsent(BiolithState.getPersistentStateType(world));
                    END.serverReplaced(END_STATE, world);
                } else {
                    Biolith.LOGGER.warn("More than one End dimension world created; cowardly ignoring '{}' in favor of '{}'", world.dimension().identifier(), END_STATE.getWorldId());
                }
            } else if (BuiltinDimensionTypes.NETHER.equals(dimensionKey.get())) {
                if (NETHER_STATE == null) {
                    NETHER_STATE = world.getDataStorage().computeIfAbsent(BiolithState.getPersistentStateType(world));
                    NETHER.serverReplaced(NETHER_STATE, world);
                } else {
                    Biolith.LOGGER.warn("More than one Nether dimension world created; cowardly ignoring '{}' in favor of '{}'", world.dimension().identifier(), NETHER_STATE.getWorldId());
                }
            } else if (BuiltinDimensionTypes.OVERWORLD.equals(dimensionKey.get())) {
                if (OVERWORLD_STATE == null) {
                    OVERWORLD_STATE = world.getDataStorage().computeIfAbsent(BiolithState.getPersistentStateType(world));
                    OVERWORLD.serverReplaced(OVERWORLD_STATE, world);
                } else {
                    Biolith.LOGGER.warn("More than one Overworld dimension world created; cowardly ignoring '{}' in favor of '{}'", world.dimension().identifier(), OVERWORLD_STATE.getWorldId());
                }
            } else {
                Biolith.LOGGER.info("Ignoring world '{}'; unknown dimension type: {}", world.dimension().identifier(), dimensionKey.get().identifier());
            }
        } else {
            Biolith.LOGGER.info("Ignoring world '{}'; world has no associated dimension", world.dimension().identifier());
        }
    }

    public static void handleServerStopped(MinecraftServer server) {
        serverStarted = false;
        registryManager = null;
        dimensionLookup = null;
        biomeLookup = null;

        END_STATE = null;
        NETHER_STATE = null;
        OVERWORLD_STATE = null;

        END.serverStopped();
        NETHER.serverStopped();
        OVERWORLD.serverStopped();
    }
}
