package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public class VanillaCompat {
    @SuppressWarnings("unchecked")
    // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
    public static BiolithFittestNodes<RegistryEntry<Biome>> getBiome(MultiNoiseUtil.NoiseValuePoint noisePoint, MultiNoiseUtil.Entries<RegistryEntry<Biome>> entries) {
        return entries.tree.biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);
    }

    public static BiolithFittestNodes<RegistryEntry<Biome>> getEndBiome(MultiNoiseUtil.NoiseValuePoint noisePoint, MultiNoiseUtil.Entries<RegistryEntry<Biome>> entries, RegistryEntry<Biome> original) {
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes;

        if (original.matchesKey(BiomeKeys.THE_END)) {
            // We do not use noise to replace the central End biome; replacements must be explicit.
            // As such, there is no second-best-fit biome.
            MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> ultimate = new MultiNoiseUtil.SearchTree.TreeLeafNode<>(
                    new MultiNoiseUtil.NoiseHypercube(
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.temperatureNoise())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.humidityNoise())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.continentalnessNoise())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.erosionNoise())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.depth())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.weirdnessNoise())),
                            0L),
                    original);
            fittestNodes = new BiolithFittestNodes<>(ultimate, 0);
        } else {
            // Evaluate the best fit biome by noise at the noise point.
            // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
            //noinspection unchecked
            fittestNodes = entries.tree.biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);
        }

        // If the best noise fit was a vanilla biome, let whatever vanilla picked leak through.
        // This way if other mods have directly modified vanilla biome selection, it may still work.
        if (!original.equals(fittestNodes.ultimate().value) && (
                fittestNodes.ultimate().value.matchesKey(BiomeKeys.SMALL_END_ISLANDS) ||
                        fittestNodes.ultimate().value.matchesKey(BiomeKeys.END_BARRENS) ||
                        fittestNodes.ultimate().value.matchesKey(BiomeKeys.END_MIDLANDS) ||
                        fittestNodes.ultimate().value.matchesKey(BiomeKeys.END_HIGHLANDS))) {

            fittestNodes = new BiolithFittestNodes<>(
                    new MultiNoiseUtil.SearchTree.TreeLeafNode<>(createNoiseHypercube(fittestNodes.ultimate().parameters), original),
                    0L,
                    fittestNodes.ultimate(),
                    fittestNodes.ultimateDistance()
            );
        }

        return fittestNodes;
    }

    // This is a smoothed version of vanilla's End biome placement.
    public static RegistryEntry<Biome> getOriginalEndBiome(int biomeX, int biomeY, int biomeZ, MultiNoiseUtil.MultiNoiseSampler noise) {
        RegistryEntry<Biome> biomeEntry;

        int x = BiomeCoords.toBlock(biomeX);
        int y = BiomeCoords.toBlock(biomeY);
        int z = BiomeCoords.toBlock(biomeZ);

        if (MathHelper.square((long) ChunkSectionPos.getSectionCoord(x)) +
                MathHelper.square((long) ChunkSectionPos.getSectionCoord(z)) <= 4096L) {
            biomeEntry = BiomeCoordinator.getBiomeLookupOrThrow().getOrThrow(BiomeKeys.THE_END);
        } else {
            double erosion = noise.erosion().sample(new DensityFunction.UnblendedNoisePos(x, y, z));
            if (erosion > 0.25) {
                biomeEntry = BiomeCoordinator.getBiomeLookupOrThrow().getOrThrow(BiomeKeys.END_HIGHLANDS);
            } else if (erosion >= -0.0625) {
                biomeEntry = BiomeCoordinator.getBiomeLookupOrThrow().getOrThrow(BiomeKeys.END_MIDLANDS);
            } else if (erosion < -0.21875) {
                biomeEntry = BiomeCoordinator.getBiomeLookupOrThrow().getOrThrow(BiomeKeys.SMALL_END_ISLANDS);
            } else {
                biomeEntry = BiomeCoordinator.getBiomeLookupOrThrow().getOrThrow(BiomeKeys.END_BARRENS);
            }
        }

        return biomeEntry;
    }

    private static MultiNoiseUtil.NoiseHypercube createNoiseHypercube(MultiNoiseUtil.ParameterRange... parameters) {
        assert parameters.length == 6;
        return MultiNoiseUtil.createNoiseHypercube(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], parameters[5], 0L);
    }
}
