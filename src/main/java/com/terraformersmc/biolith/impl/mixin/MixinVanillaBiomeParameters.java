package com.terraformersmc.biolith.impl.mixin;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(VanillaBiomeParameters.class)
public class MixinVanillaBiomeParameters {
    @Inject(method = "writeOverworldBiomeParameters", at = @At("TAIL"))
    private void biolith$injectOverworldBiomes(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters, CallbackInfo ci) {
        BiomeCoordinator.OVERWORLD.writeBiomeParameters(parameters);
    }
}
