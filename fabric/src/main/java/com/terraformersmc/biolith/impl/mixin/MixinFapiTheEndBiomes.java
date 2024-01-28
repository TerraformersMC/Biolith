package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.terraformersmc.biolith.api.biome.SubBiomeMatcher;
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
    private static void biolith$scrapeEndMainIslandReplacement(RegistryKey<Biome> variant, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(BiomeKeys.THE_END, variant, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addHighlandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndHighlandsReplacement(RegistryKey<Biome> variant, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(BiomeKeys.END_HIGHLANDS, variant, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addSmallIslandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndSmallIslandsReplacement(RegistryKey<Biome> variant, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(BiomeKeys.SMALL_END_ISLANDS, variant, weight / (1d + weight));
        ci.cancel();
    }

    @Inject(method = "addMidlandsBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeEndMidlandsReplacement(RegistryKey<Biome> highlands, RegistryKey<Biome> variant, double weight, CallbackInfo ci) {
        BiomePlacement.addSubEnd(BiomeKeys.END_MIDLANDS, variant,
                SubBiomeMatcher.of(SubBiomeMatcher.Criterion.ofAlternate(SubBiomeMatcher.CriterionTargets.ALTERNATE, highlands, BiomeKeys.END_HIGHLANDS, false)));
        ci.cancel();
    }

    @Inject(method = "addBarrensBiome", at = @At("HEAD"), cancellable = true)
    private static void biolith$scrapeBarrensReplacement(RegistryKey<Biome> highlands, RegistryKey<Biome> variant, double weight, CallbackInfo ci) {
        BiomePlacement.addSubEnd(BiomeKeys.END_BARRENS, variant,
                SubBiomeMatcher.of(SubBiomeMatcher.Criterion.ofAlternate(SubBiomeMatcher.CriterionTargets.ALTERNATE, highlands, BiomeKeys.END_HIGHLANDS, false)));
        ci.cancel();
    }
}
