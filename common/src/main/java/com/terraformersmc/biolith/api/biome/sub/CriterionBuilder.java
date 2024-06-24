package com.terraformersmc.biolith.api.biome.sub;

import com.terraformersmc.biolith.impl.biome.sub.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.List;

/**
 * CriterionBuilder creates a {@link Criterion} (condition) which must match in order for a sub-biome
 * to replace a biome.  A criterion can also be a list of criteria, all or any of which must match.
 * Anywhere the provided criterion is met, the biome will be replaced by the sub-biome.
 * <p></p>
 * Writing noise matchers takes a lot of trial and error.  In a hurry, the built-in matchers may provide some relief:
 * <ul>
 * <li>NEAR_BORDER - Tries to target a fairly thin band around the edge of the target biome</li>
 * <li>NEAR_INTERIOR - Targets noise values near the center of the biome's noise range</li>
 * <li>BEACHSIDE - Targets the edge of a biome near an adjacent beach</li>
 * <li>OCEANSIDE - Targets the edge of a biome near an adjacent ocean</li>
 * <li>RIVERSIDE - Targets the edge of a biome near an adjacent river</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class CriterionBuilder {
    public static final Criterion NEAR_BORDER = ratioMax(RatioTarget.EDGE, 0.2f);
    public static final Criterion NEAR_INTERIOR = ratioMax(RatioTarget.CENTER, 0.2f);

    public static final Criterion BEACHSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_BEACH));
    public static final Criterion OCEANSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_OCEAN));
    public static final Criterion RIVERSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_RIVER));

    /**
     * Creates a container {@link Criterion} which inverts the match of its contained criterion.
     *
     * @param criterion A criterion to invert
     * @return The created {@link NotCriterion}
     */
    public static Criterion not(Criterion criterion) {
        return new NotCriterion(criterion);
    }

    /**
     * Creates a container {@link Criterion} which matches only if all its contained criteria match.
     *
     * @param criteria A list of criteria to check when matching
     * @return The created {@link AllOfCriterion}
     */
    public static Criterion allOf(List<Criterion> criteria) {
        return new AllOfCriterion(criteria);
    }

    /**
     * Creates a container {@link Criterion} which matches only if all its contained criteria match.
     *
     * @param criteria Any number of criteria to check when matching
     * @return The created {@link AllOfCriterion}
     */
    public static Criterion allOf(Criterion... criteria) {
        return allOf(List.of(criteria));
    }

    /**
     * Creates a container {@link Criterion} which matches if any of its contained criteria match.
     *
     * @param criteria A list of criteria to check when matching
     * @return The created {@link AnyOfCriterion}
     */
    public static Criterion anyOf(List<Criterion> criteria) {
        return new AnyOfCriterion(criteria);
    }

    /**
     * Creates a container {@link Criterion} which matches if any of its contained criteria match.
     *
     * @param criteria Any number of criteria to check when matching
     * @return The created {@link AnyOfCriterion}
     */
    public static Criterion anyOf(Criterion... criteria) {
        return anyOf(List.of(criteria));
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} used to evaluate whether the raw noise value of the
     * {@link MultiNoiseUtil.NoiseValuePoint} being evaluated is within the provided range.
     * </p><p>
     * The available noise values are detailed in {@link BiomeParameterTarget}.
     * </p>
     *
     * @param parameter {@link BiomeParameterTarget target} to evaluate
     * @param min The minimum matching value of the validity range
     * @param max The maximum matching value of the validity range
     * @return The created {@link ValueCriterion}
     */
    public static Criterion value(BiomeParameterTarget parameter, float min, float max) {
        return new ValueCriterion(parameter, min, max);
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} used to evaluate whether the raw noise value of the
     * {@link MultiNoiseUtil.NoiseValuePoint} being evaluated is at least the provided minimum.
     * </p><p>
     * The available noise values are detailed in {@link BiomeParameterTarget}.
     * </p>
     *
     * @param parameter {@link BiomeParameterTarget target} to evaluate
     * @param min The minimum matching value of the validity range
     * @return The created {@link ValueCriterion}
     */
    public static Criterion valueMin(BiomeParameterTarget parameter, float min) {
        return value(parameter, min, Float.POSITIVE_INFINITY);
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} used to evaluate whether the raw noise value of the
     * {@link MultiNoiseUtil.NoiseValuePoint} being evaluated is no more than the provided maximum.
     * </p><p>
     * The available noise values are detailed in {@link BiomeParameterTarget}.
     * </p>
     *
     * @param parameter {@link BiomeParameterTarget target} to evaluate
     * @param max The maximum matching value of the validity range
     * @return The created {@link ValueCriterion}
     */
    public static Criterion valueMax(BiomeParameterTarget parameter, float max) {
        return value(parameter, Float.NEGATIVE_INFINITY, max);
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} used to evaluate whether the distance from the center of the
     * selected biome's {@link MultiNoiseUtil.ParameterRange} to a selected noise value of the
     * {@link MultiNoiseUtil.NoiseValuePoint} being evaluated is within the provided range.
     * </p><p>
     * The available noise values are detailed in {@link BiomeParameterTarget}.
     * </p>
     *
     * @param parameter {@link BiomeParameterTarget target} to evaluate
     * @param min The minimum matching value of the validity range
     * @param max The maximum matching value of the validity range
     * @return The created {@link DeviationCriterion}
     */
    public static Criterion deviation(BiomeParameterTarget parameter, float min, float max) {
        return new DeviationCriterion(parameter, min, max);
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} used to evaluate whether the distance from the center of the
     * selected biome's {@link MultiNoiseUtil.ParameterRange} to a selected noise value of the
     * {@link MultiNoiseUtil.NoiseValuePoint} being evaluated is at least the provided minimum.
     * </p><p>
     * The available noise values are detailed in {@link BiomeParameterTarget}.
     * </p>
     *
     * @param parameter {@link BiomeParameterTarget target} to evaluate
     * @param min The minimum matching value of the validity range
     * @return The created {@link DeviationCriterion}
     */
    public static Criterion deviationMin(BiomeParameterTarget parameter, float min) {
        return deviation(parameter, min, Float.POSITIVE_INFINITY);
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} used to evaluate whether the distance from the center of the
     * selected biome's {@link MultiNoiseUtil.ParameterRange} to a selected noise value of the
     * {@link MultiNoiseUtil.NoiseValuePoint} being evaluated is no more than the provided maximum.
     * </p><p>
     * The available noise values are detailed in {@link BiomeParameterTarget}.
     * </p>
     *
     * @param parameter {@link BiomeParameterTarget target} to evaluate
     * @param max The maximum matching value of the validity range
     * @return The created {@link DeviationCriterion}
     */
    public static Criterion deviationMax(BiomeParameterTarget parameter, float max) {
        return deviation(parameter, Float.NEGATIVE_INFINITY, max);
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} to check computed ratios of the target's center and edge which can be
     * used to evaluate roughly how close the point being evaluated is to the noise center or edge of the of the
     * selected noise or replacement biome.
     * </p><p>
     * The available noise values are detailed in {@link RatioTarget}.
     * </p>
     *
     * @param target {@link RatioTarget target} to evaluate
     * @param min The minimum matching value of the validity range
     * @param max The maximum matching value of the validity range
     * @return The created {@link RatioCriterion}
     */
    public static Criterion ratio(RatioTarget target, float min, float max) {
        return new RatioCriterion(target, min, max);
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} to check computed ratios of the target's center and edge which can be
     * used to evaluate roughly how close the point being evaluated is to the noise center or edge of the of the
     * selected noise or replacement biome.
     * </p><p>
     * The available noise values are detailed in {@link RatioTarget}.
     * </p>
     *
     * @param target {@link RatioTarget target} to evaluate
     * @param min The minimum matching value of the validity range
     * @return The created {@link RatioCriterion}
     */
    public static Criterion ratioMin(RatioTarget target, float min) {
        return ratio(target, min, Float.POSITIVE_INFINITY);
    }

    /**
     * <p>
     * Creates a numerical {@link Criterion} to check computed ratios of the target's center and edge which can be
     * used to evaluate roughly how close the point being evaluated is to the noise center or edge of the of the
     * selected noise or replacement biome.
     * </p><p>
     * The available noise values are detailed in {@link RatioTarget}.
     * </p>
     *
     * @param target {@link RatioTarget target} to evaluate
     * @param max The maximum matching value of the validity range
     * @return The created {@link RatioCriterion}
     */
    public static Criterion ratioMax(RatioTarget target, float max) {
        return ratio(target, Float.NEGATIVE_INFINITY, max);
    }

    /**
     * Creates a biome {@link Criterion} to check the originally selected noise biome before replacement by Biolith
     * (can be a noise biome which was injected by Biolith) by exact match.
     *
     * @param biome The registry key of the biome being evaluated
     * @return The created {@link OriginalCriterion}
     */
    public static Criterion original(RegistryKey<Biome> biome) {
        return new OriginalCriterion(AbstractBiomeCriterion.BiomeTarget.of(biome));
    }

    /**
     * Creates a biome {@link Criterion} to check the originally selected noise biome before replacement by Biolith
     * (can be a noise biome which was injected by Biolith) by presence in a biome tag.
     *
     * @param tag The registry key of the biome tag being evaluated
     * @return The created {@link OriginalCriterion}
     */
    public static Criterion original(TagKey<Biome> tag) {
        return new OriginalCriterion(AbstractBiomeCriterion.BiomeTarget.of(tag));
    }

    /**
     * <p>
     * Creates a biome {@link Criterion} to check the next-closest biome found during noise selection
     * (can be a noise biome which was injected by Biolith) by exact match.
     * </p><p>
     * Neighbor criteria check the next-closest biome found during noise selection.  From a purely noise
     * perspective, this is the biome that is "closest" to the point being evaluated.  Unexpected results can be
     * caused by situations where one noise value varies much more rapidly than the rest.  The DEPTH noise in
     * particular is prone to this.  For example, cave biomes can be the "closest" at the surface, and the
     * neighbor of a surface biome may change not far above the surface.
     * </p>
     *
     * @param biome The registry key of the biome being evaluated
     * @return The created {@link NeighborCriterion}
     */
    public static Criterion neighbor(RegistryKey<Biome> biome) {
        return new NeighborCriterion(AbstractBiomeCriterion.BiomeTarget.of(biome));
    }

    /**
     * <p>
     * Creates a biome {@link Criterion} to check the next-closest biome found during noise selection
     * (can be a noise biome which was injected by Biolith) by presence in a biome tag.
     * </p><p>
     * Neighbor criteria check the next-closest biome found during noise selection.  From a purely noise
     * perspective, this is the biome that is "closest" to the point being evaluated.  Unexpected results can be
     * caused by situations where one noise value varies much more rapidly than the rest.  The DEPTH noise in
     * particular is prone to this.  For example, cave biomes can be the "closest" at the surface, and the
     * neighbor of a surface biome may change not far above the surface.
     * </p>
     *
     * @param tag The registry key of the biome tag being evaluated
     * @return The created {@link NeighborCriterion}
     */
    public static Criterion neighbor(TagKey<Biome> tag) {
        return new NeighborCriterion(AbstractBiomeCriterion.BiomeTarget.of(tag));
    }

    /**
     * <p>
     * Creates a biome {@link Criterion} to check the Biolith biome replacement that would have been selected,
     * as if the noise biome selected had been different, by exact match.
     * </p><p>
     * ALTERNATE criteria answer the question, "if the noise biome selected at this location had been different,
     * what would the final biome have been after replacements?"  For example, an End Midlands biome can be
     * placed adjacent to its matching End Highlands biome by replacing End Midlands when End Highlands would
     * be replaced by the matching highlands biome.
     * </p>
     *
     * @param biome The registry key of the biome being evaluated
     * @param alternate The registry key of the alternate, "as-if" biome
     * @return The created {@link AlternateCriterion}
     */
    public static Criterion alternate(RegistryKey<Biome> biome, RegistryKey<Biome> alternate) {
        return new AlternateCriterion(AbstractBiomeCriterion.BiomeTarget.of(biome), alternate);
    }

    /**
     * <p>
     * Creates a biome {@link Criterion} to check the Biolith biome replacement that would have been selected,
     * as if the noise biome selected had been different, by presence in a biome tag.
     * </p><p>
     * ALTERNATE criteria answer the question, "if the noise biome selected at this location had been different,
     * what would the final biome have been after replacements?"  For example, an End Midlands biome can be
     * placed adjacent to its matching End Highlands biome by replacing End Midlands when End Highlands would
     * be replaced by the matching highlands biome.
     * </p>
     *
     * @param tag The registry key of the biome tag being evaluated
     * @param alternate The registry key of the alternate, "as-if" biome
     * @return The created {@link AlternateCriterion}
     */
    public static Criterion alternate(TagKey<Biome> tag, RegistryKey<Biome> alternate) {
        return new AlternateCriterion(AbstractBiomeCriterion.BiomeTarget.of(tag), alternate);
    }
}
