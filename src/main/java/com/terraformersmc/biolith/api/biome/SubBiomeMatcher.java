package com.terraformersmc.biolith.api.biome;

import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SubBiomeMatcher is a configurable list of conditions which must be true in order for a sub-biome to replace a biome.
 * Anywhere all provided conditions are met, the biome will be replaced by the sub-biome.
 *
 * Writing noise matchers takes a lot of trial and error.  In a hurry, the built-in matchers may provide some relief:
 * <ul>
 * <li/> BORDER - Tries to target a fairly thin band around the edge of the target biome
 * <li/> INTERIOR - Targets noise values near the center of the biome's noise range
 * </ul>
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class SubBiomeMatcher {
    private final List<Criterion> criteria;

    public static final Criterion NEAR_BORDER = Criterion.ofMax(CriterionTargets.EDGE, CriterionTypes.RATIO, 0.2f);
    public static final Criterion NEAR_INTERIOR = Criterion.ofMax(CriterionTargets.CENTER, CriterionTypes.RATIO, 0.2f);

    public static final SubBiomeMatcher BEACHSIDE = SubBiomeMatcher.of(NEAR_BORDER, Criterion.ofBiome(CriterionTargets.NEIGHBOR, BiomeTags.IS_BEACH, false));
    public static final SubBiomeMatcher OCEANSIDE = SubBiomeMatcher.of(NEAR_BORDER, Criterion.ofBiome(CriterionTargets.NEIGHBOR, BiomeTags.IS_OCEAN, false));
    public static final SubBiomeMatcher RIVERSIDE = SubBiomeMatcher.of(NEAR_BORDER, Criterion.ofBiome(CriterionTargets.NEIGHBOR, BiomeTags.IS_RIVER, false));

    SubBiomeMatcher() {
        criteria = new ArrayList<>(8);
    }

    public static SubBiomeMatcher of(Criterion... criteria) {
        SubBiomeMatcher matcher = new SubBiomeMatcher();

        for (Criterion criterion : criteria) {
            matcher.addCriterion(criterion);
        }

        return matcher;
    }

    /**
     * CriterionTargets accepted by SubBiomeMatcher:
     * <ul><b>Target biome region</b>
     * <li/> CENTER - Compare to the center point of the target biome (RATIO only)
     * <li/> EDGE - Compare to the nearest edge of the target biome (RATIO only)
     * <li/> NEIGHBOR - Compare to the closest biome by noise (BIOME only)
     * </ul>
     * <ul><b>Original biome noise point</b> (most are DISTANCE or VALUE)
     * <li/> CONTINENTALNESS - Compare to continentalness noise
     * <li/> DEPTH - Compare to the depth value (location relative to primary surface)
     * <li/> EROSION - Compare to erosion noise
     * <li/> HUMIDITY - Compare to humidity noise
     * <li/> ORIGINAL - Compare to the original Minecraft biome (BIOME only)
     * <li/> PEAKS_VALLEYS - Compare to peaks and valleys value (derivative of weirdness)
     * <li/> TEMPERATURE - Compare to temperature noise
     * <li/> WEIRDNESS - Compare to weirdness noise
     * </ul>
     */
    public enum CriterionTargets { CONTINENTALNESS, DEPTH, EROSION, HUMIDITY, TEMPERATURE, WEIRDNESS, ORIGINAL, NEIGHBOR, PEAKS_VALLEYS, CENTER, EDGE }

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

    public SubBiomeMatcher addCriterion(Criterion criterion) {
        if (!criteria.contains(criterion)) {
            criteria.add(criterion);
        }

        return this;
    }

    public void sort() {
        criteria.sort(Comparator.comparingInt(criterion -> criterion.target.ordinal()));
    }

    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        MultiNoiseUtil.ParameterRange[] parameters = fittestNodes.ultimate().parameters;

        for (Criterion criterion : criteria) {
            switch (criterion.target) {
                /*
                 * Target biome region
                 */
                case CENTER:
                    if (criterion.checkReplacement(CriterionTargets.CENTER, fittestNodes, noisePoint, replacementRange, replacementNoise))
                        return false;
                    break;
                case EDGE:
                    if (criterion.checkReplacement(CriterionTargets.EDGE, fittestNodes, noisePoint, replacementRange, replacementNoise))
                        return false;
                    break;
                case NEIGHBOR:
                    if (criterion.checkBiome(CriterionTargets.NEIGHBOR, fittestNodes))
                        return false;
                    break;
                /*
                 * Original biome noise point
                 */
                case CONTINENTALNESS:
                    if (criterion.checkRange(criterion.type, parameters[ParameterListIndex.CONTINENTALNESS.ordinal()], noisePoint.continentalnessNoise()))
                        return false;
                    break;
                case DEPTH:
                    if (criterion.checkRange(criterion.type, parameters[ParameterListIndex.DEPTH.ordinal()], noisePoint.depth()))
                        return false;
                    break;
                case EROSION:
                    if (criterion.checkRange(criterion.type, parameters[ParameterListIndex.EROSION.ordinal()], noisePoint.erosionNoise()))
                        return false;
                    break;
                case HUMIDITY:
                    if (criterion.checkRange(criterion.type, parameters[ParameterListIndex.HUMIDITY.ordinal()], noisePoint.humidityNoise()))
                        return false;
                    break;
                case ORIGINAL:
                    if (criterion.checkBiome(CriterionTargets.ORIGINAL, fittestNodes))
                        return false;
                    break;
                case PEAKS_VALLEYS:
                    // PV is a calculated noise based on folding weirdness twice
                    long weirdnessMin = parameters[ParameterListIndex.WEIRDNESS.ordinal()].min();
                    long weirdnessMax = parameters[ParameterListIndex.WEIRDNESS.ordinal()].max();
                    long point1 = pvFromWeirdness(weirdnessMin);
                    long point2 = pvFromWeirdness(weirdnessMax);
                    long pvMin;
                    long pvMax;

                    // inflection points exist at 1/6 (2/3 of -10k), 1/2 (0), and 5/6 (2/3 of 10k) of weirdness range (-10k to 10k)
                    if (weirdnessMin < 0f && weirdnessMax > 0f) {
                        pvMin = -10000L;
                    } else {
                        pvMin = Math.min(point1, point2);
                    }
                    if ((weirdnessMin < -20000f/3f && weirdnessMax > -20000f/3f) || (weirdnessMin < 20000f/3f && weirdnessMax > 20000f/3f)) {
                        pvMax = 10000L;
                    } else {
                        pvMax = Math.max(point1, point2);
                    }

                    MultiNoiseUtil.ParameterRange pvRange = new MultiNoiseUtil.ParameterRange(pvMin, pvMax);

                    if (criterion.checkRange(criterion.type, pvRange, pvFromWeirdness(noisePoint.weirdnessNoise())))
                        return false;
                    break;
                case TEMPERATURE:
                    if (criterion.checkRange(criterion.type, parameters[ParameterListIndex.TEMPERATURE.ordinal()], noisePoint.temperatureNoise()))
                        return false;
                    break;
                case WEIRDNESS:
                    if (criterion.checkRange(criterion.type, parameters[ParameterListIndex.WEIRDNESS.ordinal()], noisePoint.weirdnessNoise()))
                        return false;
                    break;
            }
        }
        return true;
    }

    protected static long pvFromWeirdness(long weirdness) {
        /*
         * This is an approximation (more accurate) of:
         *     return MultiNoiseUtil.toLong(DensityFunctions.getPeaksValleysNoise(MultiNoiseUtil.toFloat(weirdness)));
         *
         * 1.19.3 defines getPeaksValleysNoise() as follows:
         *     public static float getPeaksValleysNoise(float weirdness) {
         *         return -(Math.abs(Math.abs(weirdness) - 0.6666667f) - 0.33333334f) * 3.0f;
         *     }
         *
         * I'd rather just call Mojang's code, but this is quite a bit faster...
         */
        return 10000L - Math.abs(Math.abs(weirdness * 3L) - 20000L);
    }

    public record Criterion(CriterionTargets target, CriterionTypes type, RegistryKey<Biome> biome, TagKey<Biome> biomeTag, float min, float max, boolean invert) {
        public Criterion {
            switch (type) {
                case BIOME:
                    if (target != CriterionTargets.NEIGHBOR && target != CriterionTargets.ORIGINAL) {
                        throw new IllegalArgumentException("Criterion type BIOME must have targets NEIGHBOR or ORIGINAL.");
                    }
                    break;
                case RATIO:
                    if (target != CriterionTargets.CENTER && target != CriterionTargets.EDGE) {
                        throw new IllegalArgumentException("Criterion type RATIO must have targets CENTER or EDGE.");
                    }
                    break;
                case DISTANCE:
                case VALUE:
                    if (target == CriterionTargets.CENTER || target == CriterionTargets.EDGE || target == CriterionTargets.NEIGHBOR || target == CriterionTargets.ORIGINAL) {
                        throw new IllegalArgumentException("Criterion types DISTANCE and VALUE cannot have targets CENTER, EDGE, NEIGHBOR, or ORIGINAL.");
                    }
                    break;
                }
            }

        // Numerical comparison targets
        public static Criterion ofRange(CriterionTargets target, CriterionTypes type, float min, float max, boolean invert) {
            return new Criterion(target, type, null, null, min, max, invert);
        }
        public static Criterion ofMin(CriterionTargets target, CriterionTypes type, float min) {
            return new Criterion(target, type, null, null, min, Float.MAX_VALUE, false);
        }
        public static Criterion ofMax(CriterionTargets target, CriterionTypes type, float max) {
            return new Criterion(target, type, null, null, Float.MIN_VALUE, max, false);
        }

        // Biome comparison targets
        public static Criterion ofBiome(CriterionTargets target, RegistryKey<Biome> biome, boolean invert) {
            return new Criterion(target, CriterionTypes.BIOME, biome, null, Float.MIN_VALUE, Float.MAX_VALUE, invert);
        }
        public static Criterion ofBiome(CriterionTargets target, TagKey<Biome> biomeTag, boolean invert) {
            return new Criterion(target, CriterionTypes.BIOME, null, biomeTag, Float.MIN_VALUE, Float.MAX_VALUE, invert);
        }

        /*
         * NOTE: The astute observer may note the meaning of the invert field seems to be ... inverted.
         *       This is not an accident; the following "check" methods return false for a match because
         *       doing so avoids the requirement to invert all the tests in the table method above.
         */

        public boolean checkBiome(CriterionTargets target, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes) {
            RegistryEntry<Biome> comparable = null;

            if (target == CriterionTargets.ORIGINAL) {
                comparable = fittestNodes.ultimate().value;
            } else if (target == CriterionTargets.NEIGHBOR) {
                if (fittestNodes.penultimate() != null) {
                    comparable = fittestNodes.penultimate().value;
                }
            }

            return invert == (comparable != null && (
                    (biome != null && comparable.matchesKey(biome)) ||
                    (biomeTag != null && comparable.isIn(biomeTag))
            ));
        }

        public boolean checkRange(CriterionTypes type, MultiNoiseUtil.ParameterRange range, long value) {
            float comparable = Float.MAX_VALUE;

            if (type == CriterionTypes.DISTANCE) {
                comparable = MultiNoiseUtil.toFloat(value - parameterCenter(range));
            } else if (type == CriterionTypes.VALUE) {
                comparable = MultiNoiseUtil.toFloat(value);
            }

            return invert == (comparable >= this.min() && comparable <= this.max());
        }

        public boolean checkReplacement(CriterionTargets target, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
            float comparable = Float.MAX_VALUE;

            if (target == CriterionTargets.CENTER) {
                // Vanilla biomes pre-Biolith replacement; /10k is analogous to MultiNoiseUtil.toFloat()
                comparable = MathHelper.sqrt((float) getSquaredDistance(parametersCenterPoint(fittestNodes.ultimate().parameters), noisePoint)) / 10000f;
                // Post-replacement we need to add in the replacement noise restriction
                if (replacementRange != null) {
                    // Replacement noise at the ends of the spectrum have centers at their extremities; thus the crap.
                    // replacementRange: x is min and y is max
                    if (replacementRange.x() <= 0f) {
                        if (replacementRange.y() < 1f) {
                            comparable = Math.max(replacementNoise, comparable);
                        }
                    } else if (replacementRange.y() >= 1f) {
                        comparable = Math.max(1f - replacementNoise, comparable);
                    } else {
                        comparable = Math.max(Math.abs(replacementNoise - (replacementRange.x() + replacementRange.y()) / 2f), comparable);
                    }
                }
            } else if (target == CriterionTargets.EDGE) {
                // Vanilla biomes pre-Biolith replacement
                if (fittestNodes.penultimate() == null) {
                    comparable = 1f;
                } else if (fittestNodes.penultimateDistance() == 0) {
                    comparable = 0f;
                } else {
                    comparable = (float) (fittestNodes.penultimateDistance() - fittestNodes.ultimateDistance()) / (float) fittestNodes.penultimateDistance();
                }
                // Post-replacement
                if (replacementRange != null) {
                    // Replacement noise at the ends of the spectrum have only one edge; thus the crap.
                    // replacementRange: x is min and y is max
                    if (replacementRange.x() <= 0f) {
                        if (replacementRange.y() < 1f) {
                            comparable = Math.min(replacementRange.y() - replacementNoise, comparable);
                        }
                    } else if (replacementRange.y() >= 1f) {
                        comparable = Math.min(replacementNoise - replacementRange.x(), comparable);
                    } else {
                        comparable = Math.min(Math.min(replacementNoise - replacementRange.x(), replacementRange.y() - replacementNoise), comparable);
                    }
                }
            }

            return invert == (comparable >= this.min() && comparable <= this.max());
        }

        static long parameterCenter(MultiNoiseUtil.ParameterRange range) {
            return (range.min() + range.max()) / 2L;
        }

        static MultiNoiseUtil.NoiseValuePoint parametersCenterPoint(MultiNoiseUtil.ParameterRange[] parameters) {
            return new MultiNoiseUtil.NoiseValuePoint(
                    parameterCenter(parameters[0]),
                    parameterCenter(parameters[1]),
                    parameterCenter(parameters[2]),
                    parameterCenter(parameters[3]),
                    parameterCenter(parameters[4]),
                    parameterCenter(parameters[5])
            );
        }

        static long getSquaredDistance(MultiNoiseUtil.NoiseValuePoint point1, MultiNoiseUtil.NoiseValuePoint point2) {
            return  MathHelper.square(point1.temperatureNoise() - point2.temperatureNoise()) +
                    MathHelper.square(point1.humidityNoise() - point2.humidityNoise()) +
                    MathHelper.square(point1.continentalnessNoise() - point2.continentalnessNoise()) +
                    MathHelper.square(point1.erosionNoise() - point2.erosionNoise()) +
                    MathHelper.square(point1.depth() - point2.depth()) +
                    MathHelper.square(point1.weirdnessNoise() - point2.weirdnessNoise());
        }
    }
}
