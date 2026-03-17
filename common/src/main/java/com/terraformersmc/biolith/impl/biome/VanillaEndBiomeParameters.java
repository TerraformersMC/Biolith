package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

import java.util.function.Consumer;

import static com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement.DEFAULT_PARAMETER;
import static com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement.OUT_OF_RANGE;

public class VanillaEndBiomeParameters {
    private static final long VANILLA_OFFSET = 0L;
    private static final Climate.Parameter VANILLA_DEPTH       = Climate.Parameter.point(0f);
    private static final Climate.Parameter VANILLA_HUMIDITY    = Climate.Parameter.point(-0.5f);
    private static final Climate.Parameter VANILLA_TEMPERATURE = Climate.Parameter.point(0.2f);
    private static final Climate.Parameter VANILLA_WEIRDNESS   = Climate.Parameter.point(0f);

    // For the vanilla biomes, ONLY erosion may vary, because the shape of the terrain is entirely decided by erosion.
    // Humid and cold mod biomes will be larger and may require an offset depending on the desired size.
    public static final Climate.ParameterPoint NOISE_SMALL_END_ISLANDS = new Climate.ParameterPoint(VANILLA_TEMPERATURE, VANILLA_HUMIDITY, DEFAULT_PARAMETER, Climate.Parameter.span(-1f, -0.21875f), VANILLA_DEPTH, VANILLA_WEIRDNESS, VANILLA_OFFSET);
    public static final Climate.ParameterPoint NOISE_END_BARRENS       = new Climate.ParameterPoint(VANILLA_TEMPERATURE, VANILLA_HUMIDITY, DEFAULT_PARAMETER, Climate.Parameter.span(-0.21875f, -0.0625f), VANILLA_DEPTH, VANILLA_WEIRDNESS, VANILLA_OFFSET);
    public static final Climate.ParameterPoint NOISE_END_MIDLANDS      = new Climate.ParameterPoint(VANILLA_TEMPERATURE, VANILLA_HUMIDITY, DEFAULT_PARAMETER, Climate.Parameter.span(-0.0625f, 0.25f), VANILLA_DEPTH, VANILLA_WEIRDNESS, VANILLA_OFFSET);
    public static final Climate.ParameterPoint NOISE_END_HIGHLANDS     = new Climate.ParameterPoint(VANILLA_TEMPERATURE, VANILLA_HUMIDITY, DEFAULT_PARAMETER, Climate.Parameter.span(0.25f, 1f), VANILLA_DEPTH, VANILLA_WEIRDNESS, VANILLA_OFFSET);

    public static void writeEndBiomeParameters(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters) {
        parameters.accept(Pair.of(OUT_OF_RANGE,            Biomes.THE_END));
        parameters.accept(Pair.of(NOISE_SMALL_END_ISLANDS, Biomes.SMALL_END_ISLANDS));
        parameters.accept(Pair.of(NOISE_END_BARRENS,       Biomes.END_BARRENS));
        parameters.accept(Pair.of(NOISE_END_MIDLANDS,      Biomes.END_MIDLANDS));
        parameters.accept(Pair.of(NOISE_END_HIGHLANDS,     Biomes.END_HIGHLANDS));
    }
}
