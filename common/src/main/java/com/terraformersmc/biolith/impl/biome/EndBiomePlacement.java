package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.biolith.impl.noise.OpenSimplexNoise2;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.function.Consumer;

public class EndBiomePlacement extends DimensionBiomePlacement {
    private final double[] scale = new double[4];

    private final MultiNoiseUtil.NoiseHypercube noiseSmallEndIslands = new MultiNoiseUtil.NoiseHypercube(DEFAULT_PARAMETER, DEFAULT_PARAMETER, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-1f, -0.21875f), DEFAULT_PARAMETER, DEFAULT_PARAMETER, 0L);
    private final MultiNoiseUtil.NoiseHypercube noiseEndBarrens      = new MultiNoiseUtil.NoiseHypercube(DEFAULT_PARAMETER, DEFAULT_PARAMETER, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-0.21875f, -0.0625f), DEFAULT_PARAMETER, DEFAULT_PARAMETER, 0L);
    private final MultiNoiseUtil.NoiseHypercube noiseEndMidlands     = new MultiNoiseUtil.NoiseHypercube(DEFAULT_PARAMETER, DEFAULT_PARAMETER, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-0.0625f, 0.25f), DEFAULT_PARAMETER, DEFAULT_PARAMETER, 0L);
    private final MultiNoiseUtil.NoiseHypercube noiseEndHighlands    = new MultiNoiseUtil.NoiseHypercube(DEFAULT_PARAMETER, DEFAULT_PARAMETER, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(0.25f, 1f), DEFAULT_PARAMETER, DEFAULT_PARAMETER, 0L);

    public MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> nodeSmallEndIslands;
    public MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> nodeEndBarrens;
    public MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> nodeEndMidlands;
    public MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> nodeEndHighlands;

    private OpenSimplexNoise2 humidityNoise;
    private OpenSimplexNoise2 temperatureNoise;
    private OpenSimplexNoise2 weirdnessNoise;

    public EndBiomePlacement() {
        super();

        int configScale = Biolith.getConfigManager().getGeneralConfig().getEndReplacementScale();
        scale[0] = 256 * configScale;
        scale[1] =  64 * configScale;
        scale[2] =  16 * configScale;
        scale[3] =   4 * configScale;
    }

    @Override
    protected void serverReplaced(BiolithState state, long seed) {
        super.serverReplaced(state, seed);

        RegistryEntryLookup<Biome> biomeEntryGetter = BiomeCoordinator.getBiomeLookupOrThrow();
        nodeSmallEndIslands = new MultiNoiseUtil.SearchTree.TreeLeafNode<>(noiseSmallEndIslands, biomeEntryGetter.getOrThrow(BiomeKeys.SMALL_END_ISLANDS));
        nodeEndBarrens      = new MultiNoiseUtil.SearchTree.TreeLeafNode<>(noiseEndBarrens,      biomeEntryGetter.getOrThrow(BiomeKeys.END_BARRENS));
        nodeEndMidlands     = new MultiNoiseUtil.SearchTree.TreeLeafNode<>(noiseEndMidlands,     biomeEntryGetter.getOrThrow(BiomeKeys.END_MIDLANDS));
        nodeEndHighlands    = new MultiNoiseUtil.SearchTree.TreeLeafNode<>(noiseEndHighlands,    biomeEntryGetter.getOrThrow(BiomeKeys.END_HIGHLANDS));

        humidityNoise    = new OpenSimplexNoise2(seedlets[7]);
        temperatureNoise = new OpenSimplexNoise2(seedlets[5]);
        weirdnessNoise   = new OpenSimplexNoise2(seedlets[3]);
    }

    @Override
    public double getLocalNoise(int x, int y, int z) {
        double localNoise;

        // Four octaves to give some edge fuzz
        localNoise  = replacementNoise.sample((double)(x + seedlets[0]) / scale[0], (double)(z + seedlets[1]) / scale[0]);
        localNoise += replacementNoise.sample((double)(x + seedlets[2]) / scale[1], (double)(z + seedlets[3]) / scale[1]) / 4D;
        localNoise += replacementNoise.sample((double)(x + seedlets[4]) / scale[2], (double)(z + seedlets[5]) / scale[2]) / 16D;
        localNoise += replacementNoise.sample((double)(x + seedlets[6]) / scale[3], (double)(z + seedlets[7]) / scale[3]) / 32D;

        // Scale the result back to amplitude 1 and then normalize
        localNoise = normalize(localNoise / 1.3125D);

        return localNoise;
    }

    // TODO: Should use DimensionBiomePlacement method instead,
    //       but availability of biomeEntryGetter must be thoroughly validated first.
    public void writeBiomeParameters(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters) {
        biomesInjected = true;

        // End biomes are merged during construction of the End Biome stream.

        placementRequests.forEach(request -> parameters.accept(request.pair()));

        // Replacement biomes are placed out-of-range so they do not generate except as replacements.
        // This adds the biome to TheEndBiomeSource and BiomeSource so features and structures will place.

        replacementRequests.values().stream()
                .flatMap(requestSet -> requestSet.requests.stream())
                .map(ReplacementRequest::biome).distinct()
                .forEach(biome -> {
                    if (!biome.equals(VANILLA_PLACEHOLDER)) {
                        parameters.accept(Pair.of(OUT_OF_RANGE, biome));
                    }
                });

        subBiomeRequests.values().stream()
                .flatMap(requestSet -> requestSet.requests.stream())
                .map(SubBiomeRequest::biome).distinct()
                .forEach(biome -> parameters.accept(Pair.of(OUT_OF_RANGE, biome)));
    }

    // TODO: This should be replaced with a more robust noise implementation, perhaps also more similar to vanilla.
    public MultiNoiseUtil.NoiseValuePoint sampleEndNoise(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler originalNoise, RegistryEntry<Biome> originalBiome) {
        double erosion = originalNoise.erosion().sample(new DensityFunction.UnblendedNoisePos(
                (ChunkSectionPos.getSectionCoord(BiomeCoords.toBlock(x)) * 2 + 1) * 8,
                BiomeCoords.toBlock(y),
                (ChunkSectionPos.getSectionCoord(BiomeCoords.toBlock(z)) * 2 + 1) * 8
        ));

        return new MultiNoiseUtil.NoiseValuePoint(
                MultiNoiseUtil.toLong(temperatureNoise.sample(x / 576d, z / 576d)),
                MultiNoiseUtil.toLong(humidityNoise.sample(x / 448d, z / 448d)),
                originalBiome.matchesKey(BiomeKeys.THE_END) ? 0L : MultiNoiseUtil.toLong(MathHelper.clamp((float) erosion + 0.0625f, -1f, 1f)),
                MultiNoiseUtil.toLong((float) erosion),
                156L * (56 - y),
                MultiNoiseUtil.toLong(weirdnessNoise.sample(x / 192d, z / 192d))
        );
    }
}
