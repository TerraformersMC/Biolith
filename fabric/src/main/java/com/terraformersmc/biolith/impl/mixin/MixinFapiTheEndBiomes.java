package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.terraformersmc.biolith.api.biome.sub.CriterionBuilder;
import net.fabricmc.fabric.api.biome.v1.TheEndBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TheEndBiomes.class)
public class MixinFapiTheEndBiomes {
    @Inject(method = "addMainIslandBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndMainIslandReplacement(ResourceKey<Biome> biome, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(Biomes.THE_END, biome, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addHighlandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndHighlandsReplacement(ResourceKey<Biome> biome, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(Biomes.END_HIGHLANDS, biome, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addSmallIslandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndSmallIslandsReplacement(ResourceKey<Biome> biome, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(Biomes.SMALL_END_ISLANDS, biome, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addMidlandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndMidlandsReplacement(ResourceKey<Biome> highlands, ResourceKey<Biome> midlands, double weight, CallbackInfo ci) {
        BiomePlacement.addSubEnd(Biomes.END_MIDLANDS, midlands, CriterionBuilder.alternate(highlands, Biomes.END_HIGHLANDS));
        ci.cancel();
    }

    @Inject(method = "addBarrensBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeBarrensReplacement(ResourceKey<Biome> highlands, ResourceKey<Biome> barrens, double weight, CallbackInfo ci) {
        BiomePlacement.addSubEnd(Biomes.END_BARRENS, barrens, CriterionBuilder.alternate(highlands, Biomes.END_HIGHLANDS));
        ci.cancel();
    }
}
