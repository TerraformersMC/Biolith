package com.terraformersmc.biolith.impl.surface;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.impl.platform.Services;
import com.terraformersmc.biolith.impl.surface.rule.BiomeRules;
import com.terraformersmc.biolith.impl.surface.rule.ClimateRules;
import com.terraformersmc.biolith.impl.surface.rule.MiscRules;
import com.terraformersmc.biolith.impl.surface.rule.NoiseRules;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.function.BiConsumer;

public class BiolithMaterialConditions {

	public static void init() {
		register(BiolithMaterialConditions::register);
	}

	public static void register(BiConsumer<String, MapCodec<? extends SurfaceRules.ConditionSource>> consumer) {
		consumer.accept("configured", MiscRules.Configured.CODEC.codec());
        consumer.accept("biome_tag", BiomeRules.BiomeTag.CODEC.codec());
		consumer.accept("heightmap_biome", BiomeRules.HeightmapBiome.CODEC.codec());
		consumer.accept("heightmap_biome_tag", BiomeRules.HeightmapBiomeTag.CODEC.codec());
		consumer.accept("surface_biome", BiomeRules.SurfaceBiome.CODEC.codec());
		consumer.accept("surface_biome_tag", BiomeRules.SurfaceBiomeTag.CODEC.codec());
		consumer.accept("climate_temperature", ClimateRules.Temperature.CODEC.codec());
		consumer.accept("climate_temperature_offset", ClimateRules.TemperatureOffset.CODEC.codec());
		consumer.accept("climate_downfall", ClimateRules.Downfall.CODEC.codec());
		consumer.accept("noise_temperature", NoiseRules.Temperature.CODEC.codec());
		consumer.accept("noise_humidity", NoiseRules.Humidity.CODEC.codec());
		consumer.accept("noise_erosion", NoiseRules.Erosion.CODEC.codec());
		consumer.accept("noise_continentalness", NoiseRules.Continentalness.CODEC.codec());
		consumer.accept("noise_weirdness", NoiseRules.Weirdness.CODEC.codec());
		consumer.accept("noise_depth", NoiseRules.Depth.CODEC.codec());
		consumer.accept("noise_heightmap_depth", NoiseRules.HeightmapDepth.CODEC.codec());
	}

	public static void register(String name, MapCodec<? extends SurfaceRules.ConditionSource> codec) {
		Services.PLATFORM.registerMaterialCondition(name, codec);
	}
}