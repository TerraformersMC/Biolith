package com.terraformersmc.biolith.impl.mixin;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DataFixer;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ApiServices;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.net.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {
    @Shadow
    @Final
    private CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries;

    @Shadow
    @Final
    private Map<RegistryKey<World>, ServerWorld> worlds;

    @Inject(method = "<init>", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/MinecraftServer;combinedDynamicRegistries:Lnet/minecraft/registry/CombinedDynamicRegistries;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0,
            shift = At.Shift.AFTER))
    private void biolith$earlyCaptureRegistries(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        // We need the registries really early in case TerraBlender calls us before the Fabric server start event.
        BiomeCoordinator.setRegistryManager(combinedDynamicRegistries);
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
