package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import mod.bespectacled.modernbeta.api.world.biome.BiomeProvider;
import mod.bespectacled.modernbeta.api.world.biome.BiomeResolverBlock;
import mod.bespectacled.modernbeta.world.biome.ModernBetaBiomeSource;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ModernBetaBiomeSource.class)
public abstract class MixinMBBiomeSource extends BiomeSource {
    @Override
    public @Nullable MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$getBiomeEntries() {
        return new MultiNoiseUtil.Entries<>(this.getBiomes().stream().map(
                biomeEntry -> Pair.of(DimensionBiomePlacement.OUT_OF_RANGE, biomeEntry)
                ).toList());
    }

    @WrapOperation(method = {"getBiome", "getOceanBiome", "getDeepOceanBiome", "getCaveBiome", "getBiomeForSpawn"},
            at = @At(
                    value = "INVOKE",
                    target = "Lmod/bespectacled/modernbeta/api/world/biome/BiomeProvider;getBiome(III)Lnet/minecraft/registry/entry/RegistryEntry;"
            )
    )
    @SuppressWarnings("unused")
    private RegistryEntry<Biome> biolith$getBiome(BiomeProvider instance, int biomeX, int biomeY, int biomeZ, Operation<RegistryEntry<Biome>> operation) {
        RegistryEntry<Biome> original = operation.call(instance, biomeX, biomeY, biomeZ);

        return BiomeCoordinator.OVERWORLD.getReplacementEntry(biomeX, biomeY, biomeZ, original);
    }

    @WrapOperation(method = {"getBiomeForSpawn", "getBiomeForSurfaceGen"},
            at = @At(
                    value = "INVOKE",
                    target = "Lmod/bespectacled/modernbeta/api/world/biome/BiomeResolverBlock;getBiomeBlock(III)Lnet/minecraft/registry/entry/RegistryEntry;"
            )
    )
    @SuppressWarnings("unused")
    private RegistryEntry<Biome> biolith$getBiomeBlock(BiomeResolverBlock instance, int x, int y, int z, Operation<RegistryEntry<Biome>> operation) {
        RegistryEntry<Biome> original = operation.call(instance, x, y, z);
        int biomeX = x >> 2;
        int biomeY = y >> 2;
        int biomeZ = z >> 2;

        return BiomeCoordinator.OVERWORLD.getReplacementEntry(biomeX, biomeY, biomeZ, original);
    }

    @ModifyReturnValue(method = "biomeStream", at = @At("RETURN"))
    @SuppressWarnings("unused")
    private Stream<RegistryEntry<Biome>> biolith$injectBiomes(Stream<RegistryEntry<Biome>> original) {
        Set<RegistryEntry<Biome>> entrySet = original.collect(Collectors.toSet());

        BiomeCoordinator.OVERWORLD.writeBiomeEntries(entryPair -> entrySet.add(entryPair.getSecond()));

        return entrySet.stream();
    }
}
