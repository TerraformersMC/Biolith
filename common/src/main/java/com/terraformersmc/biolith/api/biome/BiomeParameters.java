package com.terraformersmc.biolith.api.biome;

public class BiomeParameters {

    // High numbers to account for rare instances of vanilla or worldgen projects going above / below typical noise ranges
    public static final float MIN = -10F;
    public static final float MAX = 10F;

	public static final float TEMPERATURE_0 = -1F;
	public static final float TEMPERATURE_1 = -0.45F;
	public static final float TEMPERATURE_2 = -0.15F;
	public static final float TEMPERATURE_3 = 0.2F;
	public static final float TEMPERATURE_4 = 0.55F;
	public static final float TEMPERATURE_5 = 1F;

	public static final float HUMIDITY_0 = -1F;
	public static final float HUMIDITY_1 = -0.35F;
	public static final float HUMIDITY_2 = -0.1F;
	public static final float HUMIDITY_3 = 0.1F;
	public static final float HUMIDITY_4 = 0.3F;
	public static final float HUMIDITY_5 = 1F;

	public static final float CONTINENTALNESS_MUSHROOM_FIELDS = -1.2F;
	public static final float CONTINENTALNESS_DEEP_OCEAN = -1.05F;
	public static final float CONTINENTALNESS_OCEAN = -0.455F;
	public static final float CONTINENTALNESS_COAST = -0.19F;
	public static final float CONTINENTALNESS_NEAR_INLAND = -0.11F;
	public static final float CONTINENTALNESS_MID_INLAND = 0.03F;
	public static final float CONTINENTALNESS_FAR_INLAND = 0.3F;
	public static final float CONTINENTALNESS_VERY_FAR_INLAND = 1F;

	public static final float EROSION_0 = -1F;
	public static final float EROSION_1 = -0.78F;
	public static final float EROSION_2 = -0.375F;
	public static final float EROSION_3 = -0.2225F;
	public static final float EROSION_4 = 0.05F;
	public static final float EROSION_5 = 0.45F;
	public static final float EROSION_6 = 0.55F;
	public static final float EROSION_7 = 1F;

	public static final float WEIRDNESS_MID_SLICE_NORMAL = -1.0F;
	public static final float WEIRDNESS_HIGH_SLICE_NORMAL_ASCENDING = -0.93333334F;
	public static final float WEIRDNESS_PEAK_NORMAL = -0.7666667F;
	public static final float WEIRDNESS_HIGH_SLICE_NORMAL_DESCENDING = -0.56666666F;
	public static final float WEIRDNESS_MID_SLICE_NORMAL_DESCENDING = -0.4F;
	public static final float WEIRDNESS_LOW_SLICE_NORMAL_DESCENDING = -0.26666668F;
	public static final float WEIRDNESS_VALLEY = -0.05F;
	public static final float WEIRDNESS_LOW_SLICE_VARIANT_ASCENDING = 0.05F;
	public static final float WEIRDNESS_MID_SLICE_VARIANT_ASCENDING = 0.26666668F;
	public static final float WEIRDNESS_HIGH_SLICE_VARIANT_ASCENDING = 0.4F;
	public static final float WEIRDNESS_PEAK_VARIANT = 0.56666666F;
	public static final float WEIRDNESS_HIGH_SLICE_VARIANT_DESCENDING = 0.7666667F;
	public static final float WEIRDNESS_MID_SLICE_VARIANT_DESCENDING = 0.93333334F;
	public static final float WEIRDNESS_LOW_SLICE_VARIANT_DESCENDING = 1F;
}