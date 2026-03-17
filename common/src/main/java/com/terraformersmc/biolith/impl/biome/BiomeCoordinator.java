package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.biolith.impl.platform.Services;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BiomeCoordinator {
    public static final EndBiomePlacement END = new EndBiomePlacement();
    public static final NetherBiomePlacement NETHER = new NetherBiomePlacement();
    public static final OverworldBiomePlacement OVERWORLD = new OverworldBiomePlacement();
    private static boolean registeredWithTerrablender = false;

    private static @Nullable BiolithState END_STATE;
    private static @Nullable BiolithState NETHER_STATE;
    private static @Nullable BiolithState OVERWORLD_STATE;

    private static boolean serverStarted = false;
    protected static RegistryAccess.@Nullable Frozen registryManager;

    public static boolean isServerStarted() {
        return serverStarted;
    }

    public static void setRegistryManager(LayeredRegistryAccess<RegistryLayer> combinedDynamicRegistries) {
        // Called by biolith$earlyCaptureRegistries() in MixinMinecraftServer and MixinServerLoader so we can set this really early.
        registryManager = combinedDynamicRegistries.compositeAccess();
    }

    public static RegistryAccess.@Nullable Frozen getRegistryManager() {
        return registryManager;
    }

    public static Optional<Registry<Biome>> getBiomeLookup() {
        if (registryManager == null) {
            return Optional.empty();
        }

        return registryManager.lookup(Registries.BIOME);
    }

    public static HolderGetter<Biome> getBiomeLookupOrThrow() {
        return getBiomeLookup().orElseThrow();
    }

    public static void handleServerStarting(MinecraftServer server) {
        if (registryManager == null) {
            registryManager = server.registries().compositeAccess();
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

        END_STATE = null;
        NETHER_STATE = null;
        OVERWORLD_STATE = null;

        END.serverStopped();
        NETHER.serverStopped();
        OVERWORLD.serverStopped();
    }
}
