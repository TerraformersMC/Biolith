package com.terraformersmc.biolith.api.biome;

import net.minecraft.world.level.biome.OverworldBiomeBuilder;

import java.util.Arrays;

public class BiomeParameters {

	// Used to get vanilla temperature thresholds, as shown in the debug screen
	private static final OverworldBiomeBuilder OVERWORLD_BIOME_BUILDER = new OverworldBiomeBuilder();

	// Covers missing values from OverworldBiomeBuilder
	private static final float PEAK_AND_VALLEY_4 = 0.7666667F;
	private static final float PEAK_AND_VALLEY_5 = 0.93333334F;
	private static final float PEAK_AND_VALLEY_6 = 1F;

    // High numbers to account for rare instances of worldgen going above / below typical noise ranges, which happens rarely in vanilla and more often in certain mods
    public static final float MIN = -10F;
    public static final float MAX = 10F;

	public static final float TEMPERATURE_0 = OVERWORLD_BIOME_BUILDER.getTemperatureThresholds()[0].min() / 10000F;
	public static final float TEMPERATURE_1 = OVERWORLD_BIOME_BUILDER.getTemperatureThresholds()[0].max() / 10000F;
	public static final float TEMPERATURE_2 = OVERWORLD_BIOME_BUILDER.getTemperatureThresholds()[1].max() / 10000F;
	public static final float TEMPERATURE_3 = OVERWORLD_BIOME_BUILDER.getTemperatureThresholds()[2].max() / 10000F;
	public static final float TEMPERATURE_4 = OVERWORLD_BIOME_BUILDER.getTemperatureThresholds()[3].max() / 10000F;
	public static final float TEMPERATURE_5 = OVERWORLD_BIOME_BUILDER.getTemperatureThresholds()[4].max() / 10000F;

	public static final float HUMIDITY_0 = OVERWORLD_BIOME_BUILDER.getHumidityThresholds()[0].min() / 10000F;
	public static final float HUMIDITY_1 = OVERWORLD_BIOME_BUILDER.getHumidityThresholds()[0].max() / 10000F;
	public static final float HUMIDITY_2 = OVERWORLD_BIOME_BUILDER.getHumidityThresholds()[1].max() / 10000F;
	public static final float HUMIDITY_3 = OVERWORLD_BIOME_BUILDER.getHumidityThresholds()[2].max() / 10000F;
	public static final float HUMIDITY_4 = OVERWORLD_BIOME_BUILDER.getHumidityThresholds()[3].max() / 10000F;
	public static final float HUMIDITY_5 = OVERWORLD_BIOME_BUILDER.getHumidityThresholds()[4].max() / 10000F;

	public static final float CONTINENTALNESS_MUSHROOM_FIELDS = OVERWORLD_BIOME_BUILDER.getContinentalnessThresholds()[0].min() / 10000F;
	public static final float CONTINENTALNESS_DEEP_OCEAN = OVERWORLD_BIOME_BUILDER.getContinentalnessThresholds()[0].max() / 10000F;
	public static final float CONTINENTALNESS_OCEAN = OVERWORLD_BIOME_BUILDER.getContinentalnessThresholds()[1].max() / 10000F;
	public static final float CONTINENTALNESS_COAST = OVERWORLD_BIOME_BUILDER.getContinentalnessThresholds()[2].max() / 10000F;
	public static final float CONTINENTALNESS_NEAR_INLAND = OVERWORLD_BIOME_BUILDER.getContinentalnessThresholds()[3].max() / 10000F;
	public static final float CONTINENTALNESS_MID_INLAND = OVERWORLD_BIOME_BUILDER.getContinentalnessThresholds()[4].max() / 10000F;
	public static final float CONTINENTALNESS_FAR_INLAND = OVERWORLD_BIOME_BUILDER.getContinentalnessThresholds()[5].max() / 10000F;
	public static final float CONTINENTALNESS_VERY_FAR_INLAND = OVERWORLD_BIOME_BUILDER.getContinentalnessThresholds()[6].max() / 10000F;

	public static final float EROSION_0 = OVERWORLD_BIOME_BUILDER.getErosionThresholds()[0].min() / 10000F;
	public static final float EROSION_1 = OVERWORLD_BIOME_BUILDER.getErosionThresholds()[0].max() / 10000F;
	public static final float EROSION_2 = OVERWORLD_BIOME_BUILDER.getErosionThresholds()[1].max() / 10000F;
	public static final float EROSION_3 = OVERWORLD_BIOME_BUILDER.getErosionThresholds()[2].max() / 10000F;
	public static final float EROSION_4 = OVERWORLD_BIOME_BUILDER.getErosionThresholds()[3].max() / 10000F;
	public static final float EROSION_5 = OVERWORLD_BIOME_BUILDER.getErosionThresholds()[4].max() / 10000F;
	public static final float EROSION_6 = OVERWORLD_BIOME_BUILDER.getErosionThresholds()[5].max() / 10000F;
	public static final float EROSION_7 = OVERWORLD_BIOME_BUILDER.getErosionThresholds()[6].max() / 10000F;

	public static final float WEIRDNESS_MID_SLICE_NORMAL = -PEAK_AND_VALLEY_6;
	public static final float WEIRDNESS_HIGH_SLICE_NORMAL_ASCENDING = -PEAK_AND_VALLEY_5;
	public static final float WEIRDNESS_PEAK_NORMAL = -PEAK_AND_VALLEY_4;
	public static final float WEIRDNESS_HIGH_SLICE_NORMAL_DESCENDING = -OVERWORLD_BIOME_BUILDER.getPeaksAndValleysThresholds()[3].max() / 10000F;
	public static final float WEIRDNESS_MID_SLICE_NORMAL_DESCENDING = -OVERWORLD_BIOME_BUILDER.getPeaksAndValleysThresholds()[2].max() / 10000F;
	public static final float WEIRDNESS_LOW_SLICE_NORMAL_DESCENDING = -OVERWORLD_BIOME_BUILDER.getPeaksAndValleysThresholds()[1].max() / 10000F;
	public static final float WEIRDNESS_VALLEY = -OVERWORLD_BIOME_BUILDER.getPeaksAndValleysThresholds()[0].max() / 10000F;
	public static final float WEIRDNESS_LOW_SLICE_VARIANT_ASCENDING = OVERWORLD_BIOME_BUILDER.getPeaksAndValleysThresholds()[0].max() / 10000F;
	public static final float WEIRDNESS_MID_SLICE_VARIANT_ASCENDING = OVERWORLD_BIOME_BUILDER.getPeaksAndValleysThresholds()[1].max() / 10000F;
	public static final float WEIRDNESS_HIGH_SLICE_VARIANT_ASCENDING = OVERWORLD_BIOME_BUILDER.getPeaksAndValleysThresholds()[2].max() / 10000F;
	public static final float WEIRDNESS_PEAK_VARIANT = OVERWORLD_BIOME_BUILDER.getPeaksAndValleysThresholds()[3].max() / 10000F;
	public static final float WEIRDNESS_HIGH_SLICE_VARIANT_DESCENDING = PEAK_AND_VALLEY_4;
	public static final float WEIRDNESS_MID_SLICE_VARIANT_DESCENDING = PEAK_AND_VALLEY_5;
	public static final float WEIRDNESS_LOW_SLICE_VARIANT_DESCENDING = PEAK_AND_VALLEY_6;
}