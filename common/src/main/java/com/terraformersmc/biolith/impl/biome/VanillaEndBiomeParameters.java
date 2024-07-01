package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.function.Consumer;

import static com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement.DEFAULT_PARAMETER;
import static com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement.OUT_OF_RANGE;

public class VanillaEndBiomeParameters {
    private static final long VANILLA_OFFSET = 0L;
    private static final MultiNoiseUtil.ParameterRange VANILLA_DEPTH       = MultiNoiseUtil.ParameterRange.of(0f);
    private static final MultiNoiseUtil.ParameterRange VANILLA_HUMIDITY    = MultiNoiseUtil.ParameterRange.of(-0.5f);
    private static final MultiNoiseUtil.ParameterRange VANILLA_TEMPERATURE = MultiNoiseUtil.ParameterRange.of(0.2f);
    private static final MultiNoiseUtil.ParameterRange VANILLA_WEIRDNESS   = MultiNoiseUtil.ParameterRange.of(0f);

    // For the vanilla biomes, ONLY erosion may vary, because the shape of the terrain is entirely decided by erosion.
    // Humid and cold mod biomes will be larger and may require an offset depending on the desired size.
    public static final MultiNoiseUtil.NoiseHypercube NOISE_SMALL_END_ISLANDS = new MultiNoiseUtil.NoiseHypercube(VANILLA_TEMPERATURE, VANILLA_HUMIDITY, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-1f, -0.21875f), VANILLA_DEPTH, VANILLA_WEIRDNESS, VANILLA_OFFSET);
    public static final MultiNoiseUtil.NoiseHypercube NOISE_END_BARRENS       = new MultiNoiseUtil.NoiseHypercube(VANILLA_TEMPERATURE, VANILLA_HUMIDITY, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-0.21875f, -0.0625f), VANILLA_DEPTH, VANILLA_WEIRDNESS, VANILLA_OFFSET);
    public static final MultiNoiseUtil.NoiseHypercube NOISE_END_MIDLANDS      = new MultiNoiseUtil.NoiseHypercube(VANILLA_TEMPERATURE, VANILLA_HUMIDITY, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(-0.0625f, 0.25f), VANILLA_DEPTH, VANILLA_WEIRDNESS, VANILLA_OFFSET);
    public static final MultiNoiseUtil.NoiseHypercube NOISE_END_HIGHLANDS     = new MultiNoiseUtil.NoiseHypercube(VANILLA_TEMPERATURE, VANILLA_HUMIDITY, DEFAULT_PARAMETER, MultiNoiseUtil.ParameterRange.of(0.25f, 1f), VANILLA_DEPTH, VANILLA_WEIRDNESS, VANILLA_OFFSET);

    public static void writeEndBiomeParameters(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters) {
        parameters.accept(Pair.of(OUT_OF_RANGE,            BiomeKeys.THE_END));
        parameters.accept(Pair.of(NOISE_SMALL_END_ISLANDS, BiomeKeys.SMALL_END_ISLANDS));
        parameters.accept(Pair.of(NOISE_END_BARRENS,       BiomeKeys.END_BARRENS));
        parameters.accept(Pair.of(NOISE_END_MIDLANDS,      BiomeKeys.END_MIDLANDS));
        parameters.accept(Pair.of(NOISE_END_HIGHLANDS,     BiomeKeys.END_HIGHLANDS));
    }
}
