package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.terraformersmc.biolith.api.biome.subbiome.CriteriaBuilder;
import com.terraformersmc.biolith.api.biome.subbiome.CriteriaTypes;
import net.fabricmc.fabric.api.biome.v1.TheEndBiomes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TheEndBiomes.class)
public class MixinFapiTheEndBiomes {
    @Inject(method = "addMainIslandBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndMainIslandReplacement(RegistryKey<Biome> biome, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(BiomeKeys.THE_END, biome, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addHighlandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndHighlandsReplacement(RegistryKey<Biome> biome, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(BiomeKeys.END_HIGHLANDS, biome, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addSmallIslandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndSmallIslandsReplacement(RegistryKey<Biome> biome, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(BiomeKeys.SMALL_END_ISLANDS, biome, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addMidlandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndMidlandsReplacement(RegistryKey<Biome> highlands, RegistryKey<Biome> midlands, double weight, CallbackInfo ci) {
        BiomePlacement.addSubEnd(BiomeKeys.END_MIDLANDS, midlands, CriteriaBuilder.alternate(highlands, BiomeKeys.END_HIGHLANDS));
        ci.cancel();
    }

    @Inject(method = "addBarrensBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeBarrensReplacement(RegistryKey<Biome> highlands, RegistryKey<Biome> barrens, double weight, CallbackInfo ci) {
        BiomePlacement.addSubEnd(BiomeKeys.END_BARRENS, barrens, CriteriaBuilder.alternate(highlands, BiomeKeys.END_HIGHLANDS));
        ci.cancel();
    }
}
