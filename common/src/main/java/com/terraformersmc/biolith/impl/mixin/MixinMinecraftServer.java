package com.terraformersmc.biolith.impl.mixin;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.level.ServerWorldProperties;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;

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

    @Inject(method = "createWorlds", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void biolith$prependSurfaceRules(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci, ServerWorldProperties serverWorldProperties, boolean isDebug, Registry<DimensionOptions> dimensionOptionsRegistry) {
        MaterialRules.MaterialRule[] rulesType = new MaterialRules.MaterialRule[0];

        for (World world : worlds.values()) {
            DimensionOptions dimensionOptions = null;
            SurfaceRuleCollector surfaceRuleCollector = null;

            // TODO: Consider whether we need to guard against modifying the same ChunkGeneratorSettings more than once...
            if (DimensionTypes.OVERWORLD.equals(world.getDimensionKey())) {
                dimensionOptions = dimensionOptionsRegistry.get(DimensionOptions.OVERWORLD);
                surfaceRuleCollector = SurfaceRuleCollector.OVERWORLD;
            } else if (DimensionTypes.THE_NETHER.equals(world.getDimensionKey())) {
                dimensionOptions = dimensionOptionsRegistry.get(DimensionOptions.NETHER);
                surfaceRuleCollector = SurfaceRuleCollector.NETHER;
            } else if (DimensionTypes.THE_END.equals(world.getDimensionKey())) {
                dimensionOptions = dimensionOptionsRegistry.get(DimensionOptions.END);
                surfaceRuleCollector = SurfaceRuleCollector.END;
            }

            if (dimensionOptions != null && surfaceRuleCollector.getRuleCount() > 0) {
                ChunkGenerator chunkGenerator = dimensionOptions.chunkGenerator();
                //ChunkGenerator chunkGenerator = ((ServerChunkManager) world.getChunkManager()).threadedAnvilChunkStorage.chunkGenerator;
                if (chunkGenerator instanceof NoiseChunkGenerator noiseChunkGenerator) {
                    ChunkGeneratorSettings chunkGeneratorSettings = noiseChunkGenerator.getSettings().value();

                    ((MixinChunkGeneratorSettings)(Object) chunkGeneratorSettings).biolith$setSurfaceRule(
                            MaterialRules.sequence(Streams.concat(
                                            Arrays.stream(surfaceRuleCollector.getAll()),
                                            Stream.of(chunkGeneratorSettings.surfaceRule()))
                                    .toList().toArray(rulesType)));
                }
            }
        }
    }
}
