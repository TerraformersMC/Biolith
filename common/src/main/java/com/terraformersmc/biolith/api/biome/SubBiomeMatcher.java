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

import java.util.List;
import java.util.Objects;

/**
 * SubBiomeMatcher is a configurable list of {@link Criterion} (conditions) which must be true in order for a sub-biome
 * to replace a biome.  Anywhere all provided conditions are met, the biome will be replaced by the sub-biome.
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
     * <b>Evaluate a list of criteria</b>
     * <ul style="padding: none;">
     * <li>CRITERIA - Evaluate whether any or all of a list of criteria are true</li>
     * </ul>
     * <b>Target biome region</b>
     * <ul>
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
    public enum CriterionTargets { CRITERIA, CONTINENTALNESS, DEPTH, EROSION, HUMIDITY, TEMPERATURE, WEIRDNESS, ALTERNATE, ORIGINAL, NEIGHBOR, PEAKS_VALLEYS, CENTER, EDGE }

    /**
     * CriterionTypes accepted by SubBiomeMatcher:
     * <ul>
     * <li>ALL_OF - Evaluate whether all of a list of criteria are true</li>
     * <li>ANY_OF - Evaluate whether any of a list of criteria are true</li>
     * <li>BIOME - Compare to a specified biome (or biome tag) registry key</li>
     * <li>DISTANCE - Compare to noise distance from center of original biome noise point</li>
     * <li>RATIO - Compare to range of valid values for the original or target biome</li>
     * <li>VALUE - Compare to raw noise values for original biome noise point</li>
     * </ul>
     */
    public enum CriterionTypes { ALL_OF, ANY_OF, BIOME, DISTANCE, RATIO, VALUE }

    public enum ParameterListIndex { TEMPERATURE, HUMIDITY, CONTINENTALNESS, EROSION, DEPTH, WEIRDNESS, OFFSET }

    /**
     * Creates a sub-biome matcher to evaluate the provided criteria when placing sub-biomes.
     * All provided criteria must evaluate true in order for the matcher to match.
     *
     * @param criteria One or more {@link Criterion} to match
     * @return A matcher which will match the provided criteria
     */
    public static SubBiomeMatcher of(Criterion... criteria) {
        return SubBiomeMatcherImpl.of(criteria);
    }

    /**
     * Creates a mutable copy of an existing sub-biome matcher.
     *
     * @return Mutable copy of the instance
     */
    public abstract SubBiomeMatcher clone();

    /**
     * Adds a {@link Criterion} to an existing sub-biome matcher.
     *
     * @param criterion One sub-biome matcher criterion to add to the matcher
     * @return The modified (not copied) sub-biome matcher
     */
    public abstract SubBiomeMatcher addCriterion(Criterion criterion);

    public abstract void sort();

    /**
     * Evaluates whether a {@link Criterion} matches the provided state values.
     *
     * @param fittestNodes {@link BiolithFittestNodes} as returned by noise biome evaluation
     * @param biomePlacement {@link DimensionBiomePlacement} of the dimension being generated
     * @param noisePoint {@link MultiNoiseUtil.NoiseValuePoint} at the locus under evaluation
     * @param replacementRange Biolith replacement noise range of the selected replacement biome
     * @param replacementNoise Biolith replacement noise value at the locus under evaluation
     * @return True if all evaluated criteria match (return true)
     */
    public abstract boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise);

    /**
     * <p>
     * A SubBiomeMatcher.Criterion may be configured to match a wide variety of different conditions
     * with respect to the selection of a noise or replacement biome at a particular location in a world.
     * </p><p>
     * Each criterion has a {@link CriterionTypes type} and a {@link CriterionTargets target}.  Broadly
     * speaking, the target determines what value is compared and the type determines how it is compared.
     * Each type is limited to specific targets (see 'target' above for the specifics).
     * </p><p>
     * For example, a criterion with type BIOME and target CENTER is used to evaluate how close the
     * location under evaluation is to the multi-dimensional center of the selected biome.
     * </p>
     */
    public interface Criterion {
        CriterionTargets target();
        CriterionTypes type();

        // Grouped criteria targets

        /**
         * Creates a criterion containing a list of criteria to evaluate.  Applicable types are
         * ALL_OF (all criteria must match) and ANY_OF (at least one criterion must match).
         *
         * @param type Either ALL_OF or ANY_OF to require either all or just one criterion to match
         * @param criteria A list of {@link Criterion} to check when matching
         * @param invert If true, the overall result of matching will be inverted
         * @return The created {@link Criterion}
         */
        static Criterion ofCriteria(CriterionTypes type, List<Criterion> criteria, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(CriterionTargets.CRITERIA, type, null, null, null, Float.MIN_VALUE, Float.MAX_VALUE, criteria, invert);
        }

        /**
         * Creates a criterion containing a list of criteria to evaluate.  Applicable types are
         * ALL_OF (all criteria must match) and ANY_OF (at least one criterion must match).
         *
         * @param type Either ALL_OF or ANY_OF to require either all or just one criterion to match
         * @param invert If true, the overall result of matching will be inverted
         * @param criteria Any number of {@link Criterion} to check when matching
         * @return The created {@link Criterion}
         */
        static Criterion ofCriteria(CriterionTypes type, boolean invert, Criterion... criteria) {
            Objects.requireNonNull(criteria);
            return SubBiomeMatcher.Criterion.ofCriteria(type, List.of(criteria), invert);
        }

        // Numerical comparison targets

        /**
         * <p>
         * Creates a criterion of type DISTANCE, RATIO, or VALUE, used to evaluate whether a numerical value is
         * within a specific range.
         * </p><p>
         * DISTANCE criteria check the distance the center of the selected biome's {@link MultiNoiseUtil.ParameterRange}
         * is from a selected noise value of the {@link MultiNoiseUtil.NoiseValuePoint} being evaluated.
         * </p><p>
         * RATIO criteria check computed ratios of the targets CENTER and EDGE which can be used to evaluate roughly
         * how close the point being evaluated is to the noise center or edge of the of the selected noise or
         * replacement biome.
         * </p><p>
         * VALUE criteria check the raw noise value of the {@link MultiNoiseUtil.NoiseValuePoint} being evaluated.
         * </p><p>
         * The following noise values are avaliable as targets for DISTANCE and VALUE criteria:
         * CONTINENTALNESS, DEPTH, EROSION, HUMIDITY, PEAKS_VALLEYS, TEMPERATURE, WEIRDNESS
         * </p>
         *
         * @param target A criterion {@link CriterionTargets target} valid for the selected type
         * @param type A criterion {@link CriterionTypes type} of DISTANCE, RATIO, or VALUE
         * @param min The minimum matching value of the validity range
         * @param max The maximum matching value of the validity range
         * @param invert If true, the criterion's result will be inverted
         * @return The created {@link Criterion}
         */
        static Criterion ofRange(CriterionTargets target, CriterionTypes type, float min, float max, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(target, type, null, null, null, min, max, null, invert);
        }

        /**
         * Convenience version of {@link Criterion#ofRange} which sets the range max to {@link Float#MAX_VALUE}.
         *
         * @param target A criterion {@link CriterionTargets target} valid for the selected type
         * @param type A criterion {@link CriterionTypes type} of DISTANCE, RATIO, or VALUE
         * @param min The minimum matching value of the validity range
         * @return The created {@link Criterion}
         */
        static Criterion ofMin(CriterionTargets target, CriterionTypes type, float min) {
            return new SubBiomeMatcherImpl.Criterion(target, type, null, null, null, min, Float.MAX_VALUE, null, false);
        }

        /**
         * Convenience version of {@link Criterion#ofRange} which sets the range min to {@link Float#MIN_VALUE}.
         *
         * @param target A criterion {@link CriterionTargets target} valid for the selected type
         * @param type A criterion {@link CriterionTypes type} of DISTANCE, RATIO, or VALUE
         * @param max The maximum matching value of the validity range
         * @return The created {@link Criterion}
         */
        static Criterion ofMax(CriterionTargets target, CriterionTypes type, float max) {
            return new SubBiomeMatcherImpl.Criterion(target, type, null, null, null, Float.MIN_VALUE, max, null, false);
        }

        // Biome comparison targets

        /**
         * <p>
         * Creates a criterion of type BIOME and target NEIGHBOR or ORIGINAL, used to check the selected noise biome
         * or the next-closest noise biome by exact match.
         * </p><p>
         * NEIGHBOR criteria check the next-closest biome found during noise selection.  From a purely noise
         * perspective, this is the biome that is "closest" to the point being evaluated.  Unexpected results can be
         * caused by situations where one noise value varies much more rapidly than the rest.  The DEPTH noise in
         * particular is prone to this.  For example, cave biomes can be the "closest" at the surface, and the
         * neighbor of a surface biome may change not far above the surface.
         * </p><p>
         * ORIGINAL criteria check the originally selected noise biome before replacement by Biolith.  This can be
         * a noise biome which was injected by Biolith.
         * </p>
         *
         * @param target A criterion {@link CriterionTargets target} valid for the selected type
         * @param biome The registry key of the biome being evaluated
         * @param invert If true, the criterion's result will be inverted
         * @return The created {@link Criterion}
         */
        static Criterion ofBiome(CriterionTargets target, RegistryKey<Biome> biome, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(target, CriterionTypes.BIOME, biome, null, null, Float.MIN_VALUE, Float.MAX_VALUE, null, invert);
        }

        /**
         * <p>
         * Creates a criterion of type BIOME and target NEIGHBOR or ORIGINAL, used to check the selected noise biome
         * or the next-closest noise biome by presence in a biome tag.
         * </p><p>
         * NEIGHBOR criteria check the next-closest biome found during noise selection.  From a purely noise
         * perspective, this is the biome that is "closest" to the point being evaluated.  Unexpected results can be
         * caused by situations where one noise value varies much more rapidly than the rest.  The DEPTH noise in
         * particular is prone to this.  For example, cave biomes can be the "closest" at the surface, and the
         * neighbor of a surface biome may change not far above the surface.
         * </p><p>
         * ORIGINAL criteria check the originally selected noise biome before replacement by Biolith.  This can be
         * a noise biome which was injected by Biolith.
         * </p>
         *
         * @param target A criterion {@link CriterionTargets target} valid for the selected type
         * @param biomeTag The registry key of the biome tag being evaluated
         * @param invert If true, the criterion's result will be inverted
         * @return The created {@link Criterion}
         */
        static Criterion ofBiome(CriterionTargets target, TagKey<Biome> biomeTag, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(target, CriterionTypes.BIOME, null, null, biomeTag, Float.MIN_VALUE, Float.MAX_VALUE, null, invert);
        }

        /**
         * <p>
         * Creates a criterion of type BIOME and target ALTERNATE.
         * </p><p>
         * ALTERNATE criteria answer the question, "if the noise biome selected at this location had been different,
         * what would the final biome have been after replacements?"  For example, an End Midlands biome can be
         * placed adjacent to its matching End Highlands biome by replacing End Midlands when End Highlands would
         * be replaced by the matching highlands biome.
         * </p>
         *
         * @param biome The biome registry key of the targeted replacement biome
         * @param alternateBiome The biome registry key of the targeted original noise biome
         * @param invert If true, the criterion's result will be inverted
         * @return The created {@link Criterion}
         */
        static Criterion ofAlternate(RegistryKey<Biome> biome, RegistryKey<Biome> alternateBiome, boolean invert) {
            return new SubBiomeMatcherImpl.Criterion(CriterionTargets.ALTERNATE, CriterionTypes.BIOME, biome, alternateBiome, null, Float.MIN_VALUE, Float.MAX_VALUE, null, invert);
        }

        boolean checkBiome(CriterionTargets target, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, float replacementNoise);
        boolean checkCriteria(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise);
        boolean checkRange(CriterionTypes type, MultiNoiseUtil.ParameterRange range, long value);
        boolean checkReplacement(CriterionTargets target, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise);
    }
}
