package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.function.Consumer;

public class NetherBiomePlacement extends DimensionBiomePlacement {
    private final double[] scale = new double[5];

    public NetherBiomePlacement() {
        super();

        int configScale = Biolith.getConfigManager().getGeneralConfig().getNetherReplacementScale();
        scale[0] = 256 * configScale;
        scale[1] =  64 * configScale;
        scale[2] =  32 * configScale;
        scale[3] =   8 * configScale;
        scale[4] =   2 * configScale;
    }

    public double getLocalNoise(int x, int y, int z) {
        double localNoise;

        // Three octaves to give some edge fuzz
        localNoise  = replacementNoise.sample((double)(x + seedlets[0]) / scale[0], (double)(y + seedlets[0]) / scale[1], (double)(z + seedlets[1]) / scale[0]);
        localNoise += replacementNoise.sample((double)(x + seedlets[4]) / scale[2], (double)(y + seedlets[0]) / scale[3], (double)(z + seedlets[5]) / scale[2]) / 16D;
        localNoise += replacementNoise.sample((double)(x + seedlets[6]) / scale[3], (double)(y + seedlets[0]) / scale[4], (double)(z + seedlets[7]) / scale[3]) / 32D;

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
