package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.terraform.noise.OpenSimplexNoise2;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class EndBiomePlacement extends DimensionBiomePlacement {
    private final MultiNoiseUtil.NoiseHypercube noiseSmallEndIslands = new MultiNoiseUtil.NoiseHypercube(DEFAULT_PARAMETER, DEFAULT_PARAMETER, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-1f, -0.21875f), DEFAULT_PARAMETER, DEFAULT_PARAMETER, 0L);
    private final MultiNoiseUtil.NoiseHypercube noiseEndBarrens      = new MultiNoiseUtil.NoiseHypercube(DEFAULT_PARAMETER, DEFAULT_PARAMETER, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-0.21875f, -0.0625f), DEFAULT_PARAMETER, DEFAULT_PARAMETER, 0L);
    private final MultiNoiseUtil.NoiseHypercube noiseEndMidlands     = new MultiNoiseUtil.NoiseHypercube(DEFAULT_PARAMETER, DEFAULT_PARAMETER, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-0.0625f, 0.25f), DEFAULT_PARAMETER, DEFAULT_PARAMETER, 0L);
    private final MultiNoiseUtil.NoiseHypercube noiseEndHighlands    = new MultiNoiseUtil.NoiseHypercube(DEFAULT_PARAMETER, DEFAULT_PARAMETER, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(0.25f, 1f), DEFAULT_PARAMETER, DEFAULT_PARAMETER, 0L);

    public MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> nodeSmallEndIslands;
    public MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> nodeEndBarrens;
    public MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> nodeEndMidlands;
    public MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> nodeEndHighlands;

    public OpenSimplexNoise2 humidityNoise;
    public OpenSimplexNoise2 temperatureNoise;
    public OpenSimplexNoise2 weirdnessNoise;

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

    /*
     * Known conditions in the getReplacement functions, validated by MixinMultiNoiseBiomeSource:
     * - original != null
     * - original.hasKeyAndValue()
     */

    @Override
    public RegistryEntry<Biome> getReplacement(int x, int y, int z, MultiNoiseUtil.NoiseValuePoint noisePoint, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes) {
        RegistryEntry<Biome> biomeEntry = fittestNodes.ultimate().value;
        RegistryKey<Biome> biomeKey = biomeEntry.getKey().orElseThrow();

        double localNoise = -1D;
        Vector2f localRange = null;

        // select phase one -- direct replacements
        if (replacementRequests.containsKey(biomeKey)) {
            double locus = 0D;
            localNoise = getLocalNoise(x, y, z);

            for (ReplacementRequest request : replacementRequests.get(biomeKey).requests) {
                locus += request.scaled();
                if (locus > localNoise) {

                    localRange = new Vector2f((float) (locus - request.scaled()), (float) (locus > 0.9999f ? 1f : locus));
                    if (!request.biome().equals(VANILLA_PLACEHOLDER)) {
                        biomeEntry = request.biomeEntry();
                        biomeKey = request.biome();
                    }
                    break;
                }
            }
        }

        // select phase two -- sub-biome replacements
        if (subBiomeRequests.containsKey(biomeKey)) {
            if (localNoise < 0D) {
                localNoise = getLocalNoise(x, y, z);
            }

            for (SubBiomeRequest subRequest : subBiomeRequests.get(biomeKey).requests) {
                if (subRequest.matcher().matches(fittestNodes, noisePoint, localRange, (float) localNoise)) {
                    biomeEntry = subRequest.biomeEntry();
                    biomeKey = subRequest.biome();
                    break;
                }
            }
        }

        return biomeEntry;
    }

    private double getLocalNoise(int x, int y, int z) {
        double localNoise;

        // Four octaves to give some edge fuzz
        localNoise  = replacementNoise.sample((double)(x + seedlets[0]) / 1024D, (double)(z + seedlets[1]) / 1024D);
        localNoise += replacementNoise.sample((double)(x + seedlets[2]) /  512D, (double)(z + seedlets[3]) /  512D) / 4D;
        localNoise += replacementNoise.sample((double)(x + seedlets[4]) /  256D, (double)(z + seedlets[5]) /  256D) / 16D;
        localNoise += replacementNoise.sample((double)(x + seedlets[6]) /  128D, (double)(z + seedlets[7]) /  128D) / 32D;

        // Scale the result back to amplitude 1 and then normalize
        localNoise = normalize(localNoise / 1.3125D);

        return localNoise;
    }

    public void writeBiomeEntries(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>>> parameters) {
        biomesInjected = true;
        RegistryEntryLookup<Biome> biomeEntryGetter = BiomeCoordinator.getBiomeLookupOrThrow();

        // End biomes are merged during construction of the End Biome stream.

        placementRequests.forEach(pair -> parameters.accept(pair.mapSecond(biomeEntryGetter::getOrThrow)));

        // Replacement biomes are placed out-of-range so they do not generate except as replacements.
        // This adds the biome to TheEndBiomeSource and BiomeSource so features and structures will place.

        replacementRequests.values().stream()
                .flatMap(requestSet -> requestSet.requests.stream())
                .map(ReplacementRequest::biome).distinct()
                .forEach(biome -> {
                    if (!biome.equals(VANILLA_PLACEHOLDER)) {
                        parameters.accept(Pair.of(OUT_OF_RANGE, biomeEntryGetter.getOrThrow(biome)));
                    }
                });

        subBiomeRequests.values().stream()
                .flatMap(requestSet -> requestSet.requests.stream())
                .map(SubBiomeRequest::biome).distinct()
                .forEach(biome -> parameters.accept(Pair.of(OUT_OF_RANGE, biomeEntryGetter.getOrThrow(biome))));
    }

    // TODO: Deprecated for clean-up in the mixins -- Review and remove from all DimensionBiomePlacements?
    public void writeBiomeParameters(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters) {
        biomesInjected = true;

        // End biomes are merged during construction of the End Biome stream.

        placementRequests.forEach(parameters);

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
}
