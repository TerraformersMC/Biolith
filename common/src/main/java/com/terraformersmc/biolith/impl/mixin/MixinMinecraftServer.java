package com.terraformersmc.biolith.impl.mixin;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
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
            target = "Lnet/minecraft/core/LayeredRegistryAccess;compositeAccess()Lnet/minecraft/core/RegistryAccess$Frozen;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0
    ))
    @SuppressWarnings("unused")
    private RegistryAccess.Frozen biolith$earlyCaptureRegistries(LayeredRegistryAccess<RegistryLayer> instance, Operation<RegistryAccess.Frozen> original) {
        // This early event allows us to submit surface rules to TerraBlender before it finalizes them.
        BiomeCoordinator.handleServerPreStart(instance);

        return original.call(instance);
    }

    @WrapOperation(method = "createLevels", at = @At(
            value = "NEW",
            target = "net/minecraft/server/level/ServerLevel"
    ))
    @SuppressWarnings("unused")
    private ServerLevel biolith$prependMaterialRules(MinecraftServer server, Executor executor, LevelStorageSource.LevelStorageAccess levelStorage, ServerLevelData levelData, ResourceKey<Level> dimension, LevelStem levelStem, boolean isDebug, List<CustomSpawner> customSpawners, boolean tickTime, Operation<ServerLevel> operation) {
        Optional<ResourceKey<DimensionType>> dimensionKey = levelStem.type().unwrapKey();
        MaterialRule[] rulesType = new MaterialRule[0];
        SurfaceRuleCollector surfaceRuleCollector = null;

        if (dimensionKey.isPresent()) {
            if (BuiltinDimensionTypes.OVERWORLD.equals(dimensionKey.get())) {
                surfaceRuleCollector = SurfaceRuleCollector.OVERWORLD;
            } else if (BuiltinDimensionTypes.NETHER.equals(dimensionKey.get())) {
                surfaceRuleCollector = SurfaceRuleCollector.NETHER;
            } else if (BuiltinDimensionTypes.END.equals(dimensionKey.get())) {
                surfaceRuleCollector = SurfaceRuleCollector.END;
            }
        }

        // TODO: Consider whether we need to guard against modifying the same ChunkGeneratorSettings more than once...
        if (surfaceRuleCollector != null && surfaceRuleCollector.getRuleCount() > 0) {
            ChunkGenerator chunkGenerator = levelStem.generator();
            if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseChunkGenerator) {
                NoiseGeneratorSettings chunkGeneratorSettings = noiseChunkGenerator.generatorSettings().value();

                ((MixinNoiseGeneratorSettings)(Object) chunkGeneratorSettings).biolith$setMaterialRule(
                        Holder.direct(MaterialRules.sequence(Streams.concat(
                                Arrays.stream(surfaceRuleCollector.getAllBootstrapped(BiomeCoordinator.getRegistryAccessOrThrow())),
                                Stream.of(chunkGeneratorSettings.materialRule().value())
                        ).toList().toArray(rulesType)))
                );
            }
        }

        return operation.call(server, executor, levelStorage, levelData, dimension, levelStem, isDebug, customSpawners, tickTime);
    }
}
