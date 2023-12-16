package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.api.biome.BiomePlacement;
import net.fabricmc.fabric.impl.biome.TheEndBiomeData;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(TheEndBiomeData.class)
public class MixinFapiTheEndBiomeData {
    @Inject(method = "addEndBiomeReplacement", at = @At("TAIL"))
    private static void biolith$scrapeEndBiomeReplacement(RegistryKey<Biome> replaced, RegistryKey<Biome> variant, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(replaced, variant, Math.min(weight, 1.0d));
    }

    @Inject(method = "addEndMidlandsReplacement", at = @At("TAIL"))
    private static void biolith$scrapeEndMidlandsReplacement(RegistryKey<Biome> replaced, RegistryKey<Biome> variant, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(replaced, variant, Math.min(weight, 1.0d));
    }

    @Inject(method = "addEndBarrensReplacement", at = @At("TAIL"))
    private static void biolith$scrapeEndBarrensReplacement(RegistryKey<Biome> replaced, RegistryKey<Biome> variant, double weight, CallbackInfo ci) {
        BiomePlacement.replaceEnd(replaced, variant, Math.min(weight, 1.0d));
    }
}
