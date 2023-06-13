package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.compat.InterfaceEntries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// Inject before TerraBlender so we can ensure our tree search and placement overrides get used.
@Mixin(value = MultiNoiseBiomeSource.class, priority = 900)
public abstract class MixinMultiNoiseBiomeSource extends BiomeSource {
    @Shadow
    protected abstract MultiNoiseUtil.Entries<RegistryEntry<Biome>> getBiomeEntries();

    @Shadow @Final private Either<MultiNoiseUtil.Entries<RegistryEntry<Biome>>, RegistryEntry<MultiNoiseBiomeSourceParameterList>> biomeEntries;
    private MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$biomeEntries;

    // Inject noise points the first time somebody requests them.
    @WrapOperation(
            method = "getBiomeEntries",
            at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;")
    )
    @SuppressWarnings("unused")
    private Object biolith$injectEntries(Either<MultiNoiseUtil.Entries<RegistryEntry<Biome>>, RegistryEntry<MultiNoiseBiomeSourceParameterList>> instance, Function<MultiNoiseUtil.Entries<RegistryEntry<Biome>>, MultiNoiseUtil.Entries<RegistryEntry<Biome>>> leftMap, Function<RegistryEntry<MultiNoiseBiomeSourceParameterList>, MultiNoiseUtil.Entries<RegistryEntry<Biome>>> rightMap, Operation<Object> original) {
        synchronized (this) {
            // Only compute this once, since our version is more expensive than Mojang's.
            if (biolith$biomeEntries == null) {
                // Mojang does the exact same cast in on the return of this operation.
                //noinspection unchecked
                MultiNoiseUtil.Entries<RegistryEntry<Biome>> originalEntries =
                        (MultiNoiseUtil.Entries<RegistryEntry<Biome>>) original.call(instance, leftMap, rightMap);

                if (biolith$getDimensionType().matchesKey(DimensionTypes.OVERWORLD)) {
                    List<Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>>> parameterList = new ArrayList<>(256);

                    parameterList.addAll(originalEntries.getEntries());
                    BiomeCoordinator.OVERWORLD.writeBiomeEntries(parameterList::add);

                    biolith$biomeEntries = new MultiNoiseUtil.Entries<>(parameterList);
                } else if (biolith$getDimensionType().matchesKey(DimensionTypes.THE_NETHER)) {
                    List<Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>>> parameterList = new ArrayList<>(64);

                    parameterList.addAll(originalEntries.getEntries());
                    BiomeCoordinator.NETHER.writeBiomeEntries(parameterList::add);

                    biolith$biomeEntries = new MultiNoiseUtil.Entries<>(parameterList);
                } else {
                    biolith$biomeEntries = originalEntries;
                }
            }
        } // synchronized (this)

        return biolith$biomeEntries;
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

        if (biolith$getDimensionType().matchesKey(DimensionTypes.OVERWORLD)) {
            cir.setReturnValue(BiomeCoordinator.OVERWORLD.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else if (biolith$getDimensionType().matchesKey(DimensionTypes.THE_NETHER)) {
            cir.setReturnValue(BiomeCoordinator.NETHER.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else {
            cir.setReturnValue(fittestNodes.ultimate().value);
        }
    }
}
