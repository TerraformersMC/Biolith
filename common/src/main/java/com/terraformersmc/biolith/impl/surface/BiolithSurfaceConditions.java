package com.terraformersmc.biolith.impl.surface;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.surface.rule.BiomeRules;
import com.terraformersmc.biolith.api.surface.rule.ClimateRules;
import com.terraformersmc.biolith.api.surface.rule.MiscRules;
import com.terraformersmc.biolith.api.surface.rule.NoiseRules;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

import java.util.function.BiConsumer;

public class BiolithSurfaceConditions {

	public static void init() {
		register((name, codec) -> register(Registries.MATERIAL_CONDITION, name, codec));
	}

	public static void register(BiConsumer<String, MapCodec<? extends MaterialRules.MaterialCondition>> consumer) {
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

	public static <T> void register(Registry<T> registry, String name, T object) {
		Registry.register(registry, Biolith.key(registry.getKey(), name), object);
	}
}