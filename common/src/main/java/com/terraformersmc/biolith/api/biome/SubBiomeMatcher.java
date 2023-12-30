package com.terraformersmc.biolith.api.biome;

import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import com.terraformersmc.biolith.impl.biome.SubBiomeMatcherImpl;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

/**
 * SubBiomeMatcher is a configurable list of conditions which must be true in order for a sub-biome to replace a biome.
 * Anywhere all provided conditions are met, the biome will be replaced by the sub-biome.
 * <p></p>
 * Writing noise matchers takes a lot of trial and error.  In a hurry, the built-in matchers may provide some relief:
 * <ul>
 * <li>NEAR_BORDER - Tries to target a fairly thin band around the edge of the target biome</li>
 * <li>NEAR_INTERIOR - Targets noise values near the center of the biome's noise range</li>
 * </ul>
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public abstract class SubBiomeMatcher {
    public static final Criterion NEAR_BORDER = Criterion.ofMax(CriterionTargets.EDGE, CriterionTypes.RATIO, 0.2f);
    public static final Criterion NEAR_INTERIOR = Criterion.ofMax(CriterionTargets.CENTER, CriterionTypes.RATIO, 0.2f);

    public static final SubBiomeMatcher BEACHSIDE = SubBiomeMatcher.of(NEAR_BORDER, Criterion.ofBiome(CriterionTargets.NEIGHBOR, BiomeTags.IS_BEACH, false));
    public static final SubBiomeMatcher OCEANSIDE = SubBiomeMatcher.of(NEAR_BORDER, Criterion.ofBiome(CriterionTargets.NEIGHBOR, BiomeTags.IS_OCEAN, false));
    public static final SubBiomeMatcher RIVERSIDE = SubBiomeMatcher.of(NEAR_BORDER, Criterion.ofBiome(CriterionTargets.NEIGHBOR, BiomeTags.IS_RIVER, false));

    /**
     * CriterionTargets accepted by SubBiomeMatcher:
     * <p></p>
     * <b>Target biome region</b>
     * <ul style="padding: none;">
     * <li>ALTERNATE - Compare to biome selected for given alternate original biome (BIOME only)</li>
     * <li>CENTER - Compare to the center point of the target biome (RATIO only)</li>
     * <li>EDGE - Compare to the nearest edge of the target biome (RATIO only)</li>
     * <li>NEIGHBOR - Compare to the closest biome by noise (BIOME only)</li>
     * </ul>
     * <b>Original biome noise point</b> (most are DISTANCE or VALUE)
     * <ul>
     * <li>CONTINENTALNESS - Compare to continentalness noise</li>
     * <li>DEPTH - Compare to the depth value (location relative to primary surface)</li>
     * <li>EROSION - Compare to erosion noise</li>
     * <li>HUMIDITY - Compare to humidity noise</li>
     * <li>ORIGINAL - Compare to the original Minecraft biome (BIOME only)</li>
     * <li>PEAKS_VALLEYS - Compare to peaks and valleys value (derivative of weirdness)</li>
     * <li>TEMPERATURE - Compare to temperature noise</li>
     * <li>WEIRDNESS - Compare to weirdness noise</li>
     * </ul>
     */
    public enum CriterionTargets { CONTINENTALNESS, DEPTH, EROSION, HUMIDITY, TEMPERATURE, WEIRDNESS, ALTERNATE, ORIGINAL, NEIGHBOR, PEAKS_VALLEYS, CENTER, EDGE }

    /**
     * CriterionTypes accepted by SubBiomeMatcher:
     * <ul>
     * <li>BIOME - Compare to a specified biome (or biome tag) registry key</li>
     * <li>DISTANCE - Compare to noise distance from center of original biome noise point</li>
     * <li>RATIO - Compare to range of valid values for the original or target biome</li>
     * <li>VALUE - Compare to raw noise values for original biome noise point</li>
     * </ul>
     */
    public enum CriterionTypes { BIOME, DISTANCE, RATIO, VALUE }

    public enum ParameterListIndex { TEMPERATURE, HUMIDITY, CONTINENTALNESS, EROSION, DEPTH, WEIRDNESS, OFFSET }

    public static SubBiomeMatcher of(Criterion... criteria) {
        return SubBiomeMatcherImpl.of(criteria);
    }

    public abstract SubBiomeMatcher addCriterion(Criterion criterion);

    public abstract void sort();

    public abstract boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise);

    public interface Criterion {
        CriterionTargets target();
        CriterionTypes type();

        // Numerical comparison targets
        static Criterion ofRange(CriterionTargets target, CriterionTypes type, float min, float max, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(target, type, null, null, null, min, max, invert);
        }
        static Criterion ofMin(CriterionTargets target, CriterionTypes type, float min) {
            return new SubBiomeMatcherImpl.Criterion(target, type, null, null, null, min, Float.MAX_VALUE, false);
        }
        static Criterion ofMax(CriterionTargets target, CriterionTypes type, float max) {
            return new SubBiomeMatcherImpl.Criterion(target, type, null, null, null, Float.MIN_VALUE, max, false);
        }

        // Biome comparison targets
        static Criterion ofBiome(CriterionTargets target, RegistryKey<Biome> biome, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(target, CriterionTypes.BIOME, biome, null, null, Float.MIN_VALUE, Float.MAX_VALUE, invert);
        }
        static Criterion ofBiome(CriterionTargets target, TagKey<Biome> biomeTag, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(target, CriterionTypes.BIOME, null, null, biomeTag, Float.MIN_VALUE, Float.MAX_VALUE, invert);
        }
        static Criterion ofAlternate(CriterionTargets target, RegistryKey<Biome> biome, RegistryKey<Biome> alternateBiome, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(target, CriterionTypes.BIOME, biome, alternateBiome, null, Float.MIN_VALUE, Float.MAX_VALUE, invert);
        }

        boolean checkBiome(CriterionTargets target, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, float replacementNoise);
        boolean checkRange(CriterionTypes type, MultiNoiseUtil.ParameterRange range, long value);
        boolean checkReplacement(CriterionTargets target, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise);
    }
}
