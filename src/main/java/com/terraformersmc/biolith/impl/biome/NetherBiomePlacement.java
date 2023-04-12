package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.function.Consumer;

public class NetherBiomePlacement extends DimensionBiomePlacement {
    /*
     * Known conditions in the getReplacement functions, validated by MixinMultiNoiseBiomeSource:
     * - original != null
     * - original.hasKeyAndValue()
     */

    @Override
    public RegistryEntry<Biome> getReplacement(int x, int y, int z, MultiNoiseUtil.NoiseValuePoint noisePoint, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes) {
        RegistryEntry<Biome> original = fittestNodes.ultimate().value;
        RegistryKey<Biome> originalKey = original.getKey().orElseThrow();

        RegistryEntry<Biome> replacement = original;
        double localNoise = normalize(replacementNoise.sample((double)x / 256D, (double)y / 16D, (double)z / 256D));

        // TODO: select

        return replacement;
    }

    public void writeBiomeParameters(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters) {
        biomesInjected = true;
        // TODO: Nether replacement is more complicated
    }
}
