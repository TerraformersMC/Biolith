package com.terraformersmc.biolith.impl.mixin;

import com.mojang.datafixers.util.Either;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.compat.InterfaceEntries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Inject before TerraBlender so we can ensure our tree search and placement overrides get used.
@Mixin(value = MultiNoiseBiomeSource.class, priority = 900)
public abstract class MixinMultiNoiseBiomeSource {
    @Shadow
    protected abstract MultiNoiseUtil.Entries<RegistryEntry<Biome>> getBiomeEntries();

    @Unique
    private boolean biolith$isNether = false;
    @Unique
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
        MultiNoiseUtil.SearchTree<RegistryEntry<Biome>> searchTree = null;
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes;

        // Use the correct TerraBlender search tree if available.
        if (getBiomeEntries() instanceof InterfaceEntries) {
            // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
            //noinspection unchecked
            searchTree = getBiomeEntries().biolith$getuniqueTree(x, y, z);
        }
        // Load original search tree if none were provided by TerraBlender.
        if (searchTree == null) {
            searchTree = getBiomeEntries().tree;
        }

        // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
        //noinspection unchecked
        fittestNodes = searchTree.biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);

        if (biolith$isOverworld) {
            cir.setReturnValue(BiomeCoordinator.OVERWORLD.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else if (biolith$isNether) {
            cir.setReturnValue(BiomeCoordinator.NETHER.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else {
            cir.setReturnValue(fittestNodes.ultimate().value);
        }
    }
}
