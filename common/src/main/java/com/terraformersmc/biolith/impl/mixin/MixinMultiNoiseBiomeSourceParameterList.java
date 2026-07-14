package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This allows us to provide biomes during new game creation.
@Mixin(MultiNoiseBiomeSourceParameterList.class)
public class MixinMultiNoiseBiomeSourceParameterList {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void biolith$captureEarlyBiomeLookup(MultiNoiseBiomeSourceParameterList.Preset preset, HolderGetter<Biome> biomeLookup, CallbackInfo ci) {
        BiomeCoordinator.setEarlyBiomeLookup(biomeLookup);
    }
}
