package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class OverworldBiomePlacement extends DimensionBiomePlacement {
    /*
     * TODO: from the original plan but never implemented ... consider?
     * Removed biomes become "holes" that fall through to the underlying vanilla biome.  Added biomes will crowd others,
     * causing the most movement for those closest to them (the former newest additions).
     */

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

    double getLocalNoise(int x, int y, int z) {
        double localNoise;

        // Four octaves to give some edge fuzz
        localNoise  = replacementNoise.sample((double)(x + seedlets[0]) / 1024D, (double)(z + seedlets[1]) / 1024D);
        localNoise += replacementNoise.sample((double)(x + seedlets[2]) /  256D, (double)(z + seedlets[3]) /  256D) / 8D;
        localNoise += replacementNoise.sample((double)(x + seedlets[4]) /   64D, (double)(z + seedlets[5]) /   64D) / 16D;
        localNoise += replacementNoise.sample((double)(x + seedlets[6]) /   16D, (double)(z + seedlets[7]) /   16D) / 32D;

        // Scale the result back to amplitude 1 and then normalize
        localNoise = normalize(localNoise / 1.21875D);

        return localNoise;
    }

    // NOTE: biomeRegistry is not yet available when writeBiomeParameters() is called by VanillaBiomeParameters.
    public void writeBiomeParameters(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters) {
        biomesInjected = true;

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
