package com.terraformersmc.biolith.api.surface;

import com.terraformersmc.biolith.api.biome.BiomeParameters;
import com.terraformersmc.biolith.api.surface.rule.BiomeRuleTargets;
import com.terraformersmc.biolith.api.surface.rule.NoiseRuleTargets;
import com.terraformersmc.biolith.impl.surface.rule.BiomeRules;
import com.terraformersmc.biolith.impl.surface.rule.ClimateRules;
import com.terraformersmc.biolith.impl.surface.rule.MiscRules;
import com.terraformersmc.biolith.impl.surface.rule.NoiseRules;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

import java.util.List;

public class BiolithSurfaceRules {

    /**
     * These methods are used to check climate temperature.
     * isFreezing determines if it can snow at the current position
     * other methods accept float points or ranges to determine acceptable temperature climates
     */
	public static SurfaceRules.ConditionSource isFreezing() {
		return temperature(BiomeParameters.MIN, 0.15F + 0.001F);
	}
	public static SurfaceRules.ConditionSource temperature(float point) {
		return ClimateRules.Temperature.point(point);
	}
	public static SurfaceRules.ConditionSource temperature(float min, float max) {
		return ClimateRules.Temperature.range(min, max);
	}
	public static SurfaceRules.ConditionSource temperatureAbove(float point) {
		return ClimateRules.Temperature.above(point);
	}
	public static SurfaceRules.ConditionSource temperatureBelow(float point) {
		return ClimateRules.Temperature.below(point);
	}

    /**
     * These methods are used to check climate downfall.
     * noDownfall means that the current position cannot rain
     * other methods accept float points or ranges to determine acceptable downfall values
     */
	public static SurfaceRules.ConditionSource noDownfall() {
		return downfall(0F);
	}
	public static SurfaceRules.ConditionSource downfall(float point) {
		return ClimateRules.Downfall.point(point);
	}
	public static SurfaceRules.ConditionSource downfall(float min, float max) {
		return ClimateRules.Downfall.range(min, max);
	}
	public static SurfaceRules.ConditionSource downfallAbove(float point) {
		return ClimateRules.Downfall.above(point);
	}
	public static SurfaceRules.ConditionSource downfallBelow(float point) {
		return ClimateRules.Downfall.below(point);
	}

    /**
     * These methods determine the offset of temperature, relative to the current biome's actual temperature.
     * This can be useful to determine if the position is higher/lower than usual, as temperature decreases at higher y levels relative to base biome temperature
     */
	public static SurfaceRules.ConditionSource temperatureOffset(float min, float max) {
		return ClimateRules.TemperatureOffset.range(min, max);
	}
	public static SurfaceRules.ConditionSource temperatureOffsetAbove(float point) {
		return ClimateRules.TemperatureOffset.above(point);
	}
	public static SurfaceRules.ConditionSource temperatureOffsetBelow(float point) {
		return ClimateRules.TemperatureOffset.below(point);
	}

    /**
     * This samples depth based on heightmap.
     * It is preferable to use heightmap depth over regular depth where applicable, as heightmap depth is able to cache the noise calculation per-column and is thus more performant
     */
	public static SurfaceRules.ConditionSource heightmapDepth(float min, float max) {
		return NoiseRules.HeightmapDepth.range(min, max);
	}
	public static SurfaceRules.ConditionSource heightmapDepthAbove(float point) {
		return NoiseRules.HeightmapDepth.above(point);
	}
	public static SurfaceRules.ConditionSource heightmapDepthBelow(float point) {
		return NoiseRules.HeightmapDepth.below(point);
	}

    /**
     * These are improved methods for checking biomes.
     * Unlike vanilla, we accept both biomes or biome tags
     * Furthermore, you can use the BiomeRuleTargets enum in order to sample either the heightmap or surface biome, which is useful when trying to avoid near-surface cave biomes interfering with surface rule placement
     */
	public static SurfaceRules.ConditionSource isBiome(ResourceKey<Biome> biome) {
		return isBiome(biome, BiomeRuleTargets.ACTUAL);
	}
	public static SurfaceRules.ConditionSource isBiome(ResourceKey<Biome> biome, BiomeRuleTargets target) {
		return switch (target) {
			case ACTUAL -> SurfaceRules.isBiome(biome);
			case SURFACE -> BiomeRules.SurfaceBiome.isBiome(biome);
			case HEIGHTMAP -> BiomeRules.HeightmapBiome.isBiome(biome);
		};
	}

	public static SurfaceRules.ConditionSource isBiomeTag(TagKey<Biome> biome) {
		return isBiomeTag(biome, BiomeRuleTargets.ACTUAL);
	}
	public static SurfaceRules.ConditionSource isBiomeTag(TagKey<Biome> biome, BiomeRuleTargets target) {
		return switch (target) {
			case ACTUAL -> BiomeRules.BiomeTag.isBiomeTag(biome);
			case SURFACE -> BiomeRules.SurfaceBiomeTag.isBiomeTag(biome);
			case HEIGHTMAP -> BiomeRules.HeightmapBiomeTag.isBiomeTag(biome);
		};
	}


    /**
     * noise methods allow for placing surface rules based on biome placement noise
     * These share the same values with biome placement, including sub-biome BiomeParameterTargets
     */
	public static SurfaceRules.ConditionSource noise(NoiseRuleTargets target, float point) {
		return switch (target) {
			case TEMPERATURE -> NoiseRules.Temperature.point(point);
			case HUMIDITY -> NoiseRules.Humidity.point(point);
			case EROSION -> NoiseRules.Erosion.point(point);
			case CONTINENTALNESS -> NoiseRules.Continentalness.point(point);
			case WEIRDNESS -> NoiseRules.Weirdness.point(point);
			case DEPTH -> NoiseRules.Depth.point(point);
			case HEIGHTMAP_DEPTH -> NoiseRules.HeightmapDepth.point(point);
		};
	}
	public static SurfaceRules.ConditionSource noise(NoiseRuleTargets target, float min, float max) {
		return switch (target) {
			case TEMPERATURE -> NoiseRules.Temperature.range(min, max);
			case HUMIDITY -> NoiseRules.Humidity.range(min, max);
			case EROSION -> NoiseRules.Erosion.range(min, max);
			case CONTINENTALNESS -> NoiseRules.Continentalness.range(min, max);
			case WEIRDNESS -> NoiseRules.Weirdness.range(min, max);
			case DEPTH -> NoiseRules.Depth.range(min, max);
			case HEIGHTMAP_DEPTH -> NoiseRules.HeightmapDepth.range(min, max);
		};
	}

    /**
     * aboveDeepslate allows to easily check if a block is above deepslate (wow!), with perfect matching to deepslate transition noise
     * This can also be used to preserve performance in certain situations if doing many expensive checks such as depth noise, which is not cached per-column and thus more expensive per-block
     */
    public static SurfaceRules.ConditionSource aboveDeepslate() {
        return SurfaceRules.not(SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0), VerticalAnchor.absolute(8)));
    }

    /**
     * collectedRule allows easily creating long lists of material conditions which must pass in order to place a material rule
     * The list being reversed ensures that the top condition of the list is run first, making it easy to order least-expensive checks at the top without confusion
     */
	public static SurfaceRules.RuleSource collectedRule(List<SurfaceRules.ConditionSource> conditions, SurfaceRules.RuleSource rule) {
		if (conditions.isEmpty()) {
			return rule;
		}

		SurfaceRules.RuleSource rules = rule;

		for (int i = conditions.size() - 1; i >= 0; i--) {
			rules = SurfaceRules.ifTrue(conditions.get(i), rules);
		}

		return rules;
	}

    /**
     * configuredRule uses a boolean check in order to easily allow linking a mod config to an individual surface rule
     * you should ensure that configuredRule or checkBoolean is the first condition run when creating a material rule, in order to preserve performance by skipping later checks should the rule be disabled
     */
	public static SurfaceRules.RuleSource configuredRule(boolean config, SurfaceRules.RuleSource ruleSource) {
		return SurfaceRules.ifTrue(
			checkBoolean(config),
			ruleSource
		);
	}

	public static SurfaceRules.ConditionSource checkBoolean(boolean value) {
		return MiscRules.Configured.pass(value);
	}
}