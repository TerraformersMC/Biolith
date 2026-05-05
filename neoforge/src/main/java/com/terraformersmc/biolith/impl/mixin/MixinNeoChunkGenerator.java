package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkGenerator.class)
public abstract class MixinNeoChunkGenerator {
    @Shadow
    public abstract BiomeSource getBiomeSource();

    // The logic is, if Neo wants to regenerate features per step, then they must have modified biome entries.
    // Biolith caches its version of biome entries, so we need to regenerate that as well when they invalidate.
    @Inject(method = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;refreshFeaturesPerStep()V", at = @At("HEAD"))
    private void biolith$refreshFeatureCacheForNeo(CallbackInfo ci) {
        getBiomeSource().biolith$refreshBiomeEntries();
    }
}
