package com.terraformersmc.biolith.impl.mixin;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @WrapOperation(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/registry/CombinedDynamicRegistries;getCombinedRegistryManager()Lnet/minecraft/registry/DynamicRegistryManager$Immutable;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0
    ))
    @SuppressWarnings("unused")
    private DynamicRegistryManager.Immutable biolith$earlyCaptureRegistries(CombinedDynamicRegistries<ServerDynamicRegistryType> instance, Operation<DynamicRegistryManager.Immutable> original) {
        // This capture updates any registry manager we scraped previously with the final version.
        BiomeCoordinator.setRegistryManager(instance);

        return original.call(instance);
    }

    @WrapOperation(method = "createWorlds", at = @At(
            value = "NEW",
            target = "net/minecraft/server/world/ServerWorld"
    ))
    @SuppressWarnings("unused")
    private ServerWorld biolith$prependSurfaceRules(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState, Operation<ServerWorld> operation) {
        Optional<RegistryKey<DimensionType>> dimensionKey = dimensionOptions.dimensionTypeEntry().getKey();
        MaterialRules.MaterialRule[] rulesType = new MaterialRules.MaterialRule[0];
        SurfaceRuleCollector surfaceRuleCollector = null;

        if (dimensionKey.isPresent()) {
            if (DimensionTypes.OVERWORLD.equals(dimensionKey.get())) {
                surfaceRuleCollector = SurfaceRuleCollector.OVERWORLD;
            } else if (DimensionTypes.THE_NETHER.equals(dimensionKey.get())) {
                surfaceRuleCollector = SurfaceRuleCollector.NETHER;
            } else if (DimensionTypes.THE_END.equals(dimensionKey.get())) {
                surfaceRuleCollector = SurfaceRuleCollector.END;
            }
        }

        // TODO: Consider whether we need to guard against modifying the same ChunkGeneratorSettings more than once...
        if (surfaceRuleCollector != null && surfaceRuleCollector.getRuleCount() > 0) {
            ChunkGenerator chunkGenerator = dimensionOptions.chunkGenerator();
            if (chunkGenerator instanceof NoiseChunkGenerator noiseChunkGenerator) {
                ChunkGeneratorSettings chunkGeneratorSettings = noiseChunkGenerator.getSettings().value();

                ((MixinChunkGeneratorSettings)(Object) chunkGeneratorSettings).biolith$setSurfaceRule(
                        MaterialRules.sequence(Streams.concat(
                                        Arrays.stream(surfaceRuleCollector.getAll()),
                                        Stream.of(chunkGeneratorSettings.surfaceRule()))
                                .toList().toArray(rulesType)));
            }
        }

        return operation.call(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
}
