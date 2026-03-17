package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import mod.bluestaggo.modernerbeta.api.level.biome.BiomeProvider;
import mod.bluestaggo.modernerbeta.api.level.biome.BiomeResolverBlock;
import mod.bluestaggo.modernerbeta.api.level.cavebiome.CaveBiomeProvider;
import mod.bluestaggo.modernerbeta.level.biome.ModernBetaBiomeSource;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ModernBetaBiomeSource.class)
public abstract class MixinMBBiomeSource extends BiomeSource {
/*
    @Override
    public Climate.@Nullable ParameterList<Holder<Biome>> biolith$getBiomeEntries() {
        return new Climate.ParameterList<>(this.possibleBiomes().stream().map(
                biomeEntry -> Pair.of(DimensionBiomePlacement.OUT_OF_RANGE, biomeEntry)
                ).toList());
    }

    @WrapOperation(method = {"getNoiseBiome", "getOceanBiome", "getDeepOceanBiome", "getCaveBiome", "getBiomeForSpawn", "getBiomeForHeightGen"},
            at = @At(
                    value = "INVOKE",
                    target = "Lmod/bluestaggo/modernerbeta/api/level/biome/BiomeProvider;getBiome(III)Lnet/minecraft/core/Holder;"
            )
    )
    @SuppressWarnings("unused")
    private Holder<Biome> biolith$getBiome(BiomeProvider instance, int biomeX, int biomeY, int biomeZ, Operation<Holder<Biome>> operation) {
        Holder<Biome> original = operation.call(instance, biomeX, biomeY, biomeZ);

        return BiomeCoordinator.OVERWORLD.getReplacementEntry(biomeX, biomeY, biomeZ, original);
    }

    @WrapOperation(method = {"getCaveBiome"},
            at = @At(
                    value = "INVOKE",
                    target = "Lmod/bluestaggo/modernerbeta/api/level/cavebiome/CaveBiomeProvider;getBiome(III)Lnet/minecraft/registry/entry/RegistryEntry;"
            )
    )
    @SuppressWarnings("unused")
    private Holder<Biome> biolith$getCaveBiome(CaveBiomeProvider instance, int biomeX, int biomeY, int biomeZ, Operation<Holder<Biome>> operation) {
        Holder<Biome> original = operation.call(instance, biomeX, biomeY, biomeZ);

        // Apparently, Modern(er) Beta uses null here to indicate no cave biome was found.
        if (original == null) {
            return null;
        }

        return BiomeCoordinator.OVERWORLD.getReplacementEntry(biomeX, biomeY, biomeZ, original);
    }

    @WrapOperation(method = {"getBiomeForSpawn", "getBiomeForSurfaceGen"},
            at = @At(
                    value = "INVOKE",
                    target = "Lmod/bluestaggo/modernerbeta/api/level/biome/BiomeResolverBlock;getBiomeBlock(III)Lnet/minecraft/registry/entry/RegistryEntry;"
            )
    )
    @SuppressWarnings("unused")
    private Holder<Biome> biolith$getBiomeBlock(BiomeResolverBlock instance, int x, int y, int z, Operation<Holder<Biome>> operation) {
        Holder<Biome> original = operation.call(instance, x, y, z);
        int biomeX = x >> 2;
        int biomeY = y >> 2;
        int biomeZ = z >> 2;

        return BiomeCoordinator.OVERWORLD.getReplacementEntry(biomeX, biomeY, biomeZ, original);
    }

    @ModifyReturnValue(method = "collectPossibleBiomes", at = @At("RETURN"))
    @SuppressWarnings("unused")
    private Stream<Holder<Biome>> biolith$injectBiomes(Stream<Holder<Biome>> original) {
        Set<Holder<Biome>> entrySet = original.collect(Collectors.toSet());

        BiomeCoordinator.OVERWORLD.writeBiomeEntries(entryPair -> entrySet.add(entryPair.getSecond()));

        return entrySet.stream();
    }
*/
}
