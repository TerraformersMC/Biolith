package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class NetherBiomePlacement extends DimensionBiomePlacement {
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

        // Three octaves to give some edge fuzz
        localNoise  = replacementNoise.sample((double)(x + seedlets[0]) / 512D, (double)(y + seedlets[0]) / 128D, (double)(z + seedlets[1]) / 512D);
        localNoise += replacementNoise.sample((double)(x + seedlets[4]) /  64D, (double)(y + seedlets[0]) /  16D, (double)(z + seedlets[5]) /  64D) / 16D;
        localNoise += replacementNoise.sample((double)(x + seedlets[6]) /  16D, (double)(y + seedlets[0]) /   4D, (double)(z + seedlets[7]) /  16D) / 32D;

        // Scale the result back to amplitude 1 and then normalize
        localNoise = normalize(localNoise / 1.09375D);

        return localNoise;
    }

    public void writeBiomeEntries(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>>> parameters) {
        biomesInjected = true;
        RegistryEntryLookup<Biome> biomeEntryGetter = BiomeCoordinator.getBiomeLookupOrThrow();

        // Nether biomes are merged during construction of the Nether parameters list.

        placementRequests.forEach(pair -> parameters.accept(pair.mapSecond(biomeEntryGetter::getOrThrow)));

        // Replacement biomes are placed out-of-range so they do not generate except as replacements.
        // This adds the biome to MultiNoiseBiomeSource and BiomeSource so features and structures will place.

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

    // TODO: Unused since 1.0.0-alpha.5 -- Review and remove from all DimensionBiomePlacements?
    public void writeBiomeParameters(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters) {
        biomesInjected = true;

        // Nether biomes are merged during construction of the Nether parameters list.

        placementRequests.forEach(parameters);

        // Replacement biomes are placed out-of-range so they do not generate except as replacements.
        // This adds the biome to MultiNoiseBiomeSource and BiomeSource so features and structures will place.

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
