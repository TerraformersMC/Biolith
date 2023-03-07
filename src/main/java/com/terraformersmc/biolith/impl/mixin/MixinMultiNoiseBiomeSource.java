package com.terraformersmc.biolith.impl.mixin;

import com.mojang.datafixers.util.Either;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.InterfaceSearchTree;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public class MixinMultiNoiseBiomeSource {
    @Shadow
    private MultiNoiseUtil.Entries<RegistryEntry<Biome>> getBiomeEntries() { return null; }

    private boolean biolith$isNether = false;
    private boolean biolith$isOverworld = false;

    // We have to evaluate what world we are in a *lot* so we want these answers precomputed as booleans we can check.
    @Inject(method = "<init>", at = @At("RETURN"))
    private void biolith$MultiNoiseBiomeSource(Either<MultiNoiseUtil.Entries<RegistryEntry<Biome>>, RegistryEntry<MultiNoiseBiomeSourceParameterList>> biomeEntries, CallbackInfo ci) {
        biomeEntries.ifRight(parameterList -> {
            biolith$isNether = parameterList.matchesId(MultiNoiseBiomeSourceParameterList.Preset.NETHER.id());
            biolith$isOverworld = parameterList.matchesId(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.id());
        });
    }

    // We calculate the vanilla/datapack biome, then we apply any overlays.
    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private void biolith$getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        MultiNoiseUtil.NoiseValuePoint noisePoint = noise.sample(x, y, z);
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes =
                ((InterfaceSearchTree<RegistryEntry<Biome>>)(Object) getBiomeEntries().tree)
                        .biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);

        cir.setReturnValue((fittestNodes.ultimate()).value);

        if (biolith$isOverworld) {
            cir.setReturnValue(BiomeCoordinator.OVERWORLD.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else if (biolith$isNether) {
            cir.setReturnValue(BiomeCoordinator.NETHER.getReplacement(x, y, z, noisePoint, fittestNodes));
        }
    }
}
