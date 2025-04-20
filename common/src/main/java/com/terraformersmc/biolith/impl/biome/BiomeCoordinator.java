package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;
import terrablender.api.SurfaceRuleManager;

import java.util.Map;
import java.util.Optional;

public class BiomeCoordinator {
    public static final DimensionBiomePlacement END = new EndBiomePlacement();
    public static final DimensionBiomePlacement NETHER = new NetherBiomePlacement();
    public static final DimensionBiomePlacement OVERWORLD = new OverworldBiomePlacement();
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

        if (Biolith.COMPAT_TERRABLENDER) {
            registerWithTerrablender();
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

    // When TerraBlender is present, it ignores our surface rules in the Overworld and Nether.
    // To avoid this, we submit a duplicate registration to TerraBlender (but only once).
    private static void registerWithTerrablender() {
        if (!registeredWithTerrablender) {
            Map.of(
                    SurfaceRuleCollector.OVERWORLD, SurfaceRuleManager.RuleCategory.OVERWORLD,
                    SurfaceRuleCollector.NETHER,    SurfaceRuleManager.RuleCategory.NETHER
            ).forEach((biolithRules, terrablenderRuleCategory) -> {
                if (biolithRules.getRuleCount() > 0) {
                    for (Identifier ruleOwner : biolithRules.getRuleOwners()) {
                        if (biolithRules.getRuleCount(ruleOwner) > 0) {
                            SurfaceRuleManager.addSurfaceRules(
                                    terrablenderRuleCategory,
                                    ruleOwner.getNamespace(),
                                    biolithRules.get(ruleOwner)
                            );
                        }
                    }
                }
            });

            registeredWithTerrablender = true;
        }
    }
}
