package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.api.biome.SubBiomeMatcher;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class SubBiomeMatcherImpl extends SubBiomeMatcher {
    private final List<SubBiomeMatcher.Criterion> criteria;

    SubBiomeMatcherImpl() {
        criteria = new ArrayList<>(8);
    }

    public static SubBiomeMatcherImpl of(SubBiomeMatcher.Criterion... criteria) {
        SubBiomeMatcherImpl matcher = new SubBiomeMatcherImpl();

        for (SubBiomeMatcher.Criterion criterion : criteria) {
            matcher.addCriterion(criterion);
        }

        return matcher;
    }

    public SubBiomeMatcherImpl clone() {
        SubBiomeMatcherImpl matcher = new SubBiomeMatcherImpl();
        matcher.criteria.addAll(criteria);

        return matcher;
    }

    public SubBiomeMatcherImpl addCriterion(SubBiomeMatcher.Criterion criterion) {
        if (!criteria.contains(criterion)) {
            criteria.add(criterion);
        }

        return this;
    }

    public void sort() {
        criteria.sort(Comparator.comparingInt(criterion -> criterion.target().ordinal()));
    }

    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        return Criterion.matchesAll(criteria, fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise);
    }

    // TODO: The implementation of Criterion is messy, getting messier, and could probably be improved.
    public record Criterion(CriterionTargets target, CriterionTypes type, RegistryKey<Biome> biome, RegistryKey<Biome> secondary, TagKey<Biome> biomeTag, float min, float max, List<SubBiomeMatcher.Criterion> criteria, boolean invert) implements SubBiomeMatcher.Criterion {
        // Consistency checks go in the Criterion record constructor:
        public Criterion {
            switch (type) {
                case ALL_OF:
                case ANY_OF:
                    if (    target != CriterionTargets.CRITERIA) {
                        throw new IllegalArgumentException("Criterion types ALL_OF and ANY_OF must have target CRITERIA.");
                    }
                    Objects.requireNonNull(criteria);
                    if (criteria.isEmpty()) {
                        throw new IllegalArgumentException("Criterion types ALL_OF and ANY_OF must have one or more sub-criteria.");
                    }
                    break;
                case BIOME:
                    if (    target != CriterionTargets.ALTERNATE && target != CriterionTargets.NEIGHBOR &&
                            target != CriterionTargets.ORIGINAL) {
                        throw new IllegalArgumentException("Criterion type BIOME must have targets ALTERNATE, NEIGHBOR, or ORIGINAL.");
                    }
                    if (biome == null && biomeTag == null) {
                        throw new IllegalArgumentException("Criterion type BIOME must define biome or biomeTag.");
                    }
                    if (    target == CriterionTargets.ALTERNATE && secondary == null) {
                        throw new IllegalArgumentException("Criterion target ALTERNATE must define secondary biome.");
                    }
                    break;
                case RATIO:
                    if (    target != CriterionTargets.CENTER && target != CriterionTargets.EDGE) {
                        throw new IllegalArgumentException("Criterion type RATIO must have targets CENTER or EDGE.");
                    }
                    break;
                case DISTANCE:
                case VALUE:
                    if (    target == CriterionTargets.ALTERNATE || target == CriterionTargets.CENTER ||
                            target == CriterionTargets.CRITERIA  || target == CriterionTargets.EDGE ||
                            target == CriterionTargets.NEIGHBOR  || target == CriterionTargets.ORIGINAL) {
                        throw new IllegalArgumentException("Criterion types DISTANCE and VALUE cannot have targets ALTERNATE, CENTER, CRITERIA, EDGE, NEIGHBOR, or ORIGINAL.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown criterion type '" + type.name() + "'; this is a Biolith bug!");
                }
            }

        private static boolean matchesAll(List<SubBiomeMatcher.Criterion> criteria, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
            MultiNoiseUtil.ParameterRange[] parameters = fittestNodes.ultimate().parameters;

            for (SubBiomeMatcher.Criterion criterion : criteria) {
                switch (criterion.target()) {
                    /*
                     * Target a set of criteria
                     */
                    case CRITERIA:
                        if (criterion.checkCriteria(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise))
                            return false;
                        break;
                    /*
                     * Target biome region
                     */
                    case ALTERNATE:
                        if (criterion.checkBiome(CriterionTargets.ALTERNATE, fittestNodes, biomePlacement, replacementNoise))
                            return false;
                        break;
                    case CENTER:
                        if (criterion.checkReplacement(CriterionTargets.CENTER, fittestNodes, noisePoint, replacementRange, replacementNoise))
                            return false;
                        break;
                    case EDGE:
                        if (criterion.checkReplacement(CriterionTargets.EDGE, fittestNodes, noisePoint, replacementRange, replacementNoise))
                            return false;
                        break;
                    case NEIGHBOR:
                        if (criterion.checkBiome(CriterionTargets.NEIGHBOR, fittestNodes, biomePlacement, replacementNoise))
                            return false;
                        break;
                    /*
                     * Original biome noise point
                     */
                    case CONTINENTALNESS:
                        if (criterion.checkRange(criterion.type(), parameters[ParameterListIndex.CONTINENTALNESS.ordinal()], noisePoint.continentalnessNoise()))
                            return false;
                        break;
                    case DEPTH:
                        if (criterion.checkRange(criterion.type(), parameters[ParameterListIndex.DEPTH.ordinal()], noisePoint.depth()))
                            return false;
                        break;
                    case EROSION:
                        if (criterion.checkRange(criterion.type(), parameters[ParameterListIndex.EROSION.ordinal()], noisePoint.erosionNoise()))
                            return false;
                        break;
                    case HUMIDITY:
                        if (criterion.checkRange(criterion.type(), parameters[ParameterListIndex.HUMIDITY.ordinal()], noisePoint.humidityNoise()))
                            return false;
                        break;
                    case ORIGINAL:
                        if (criterion.checkBiome(CriterionTargets.ORIGINAL, fittestNodes, biomePlacement, replacementNoise))
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

                        if (criterion.checkRange(criterion.type(), pvRange, pvFromWeirdness(noisePoint.weirdnessNoise())))
                            return false;
                        break;
                    case TEMPERATURE:
                        if (criterion.checkRange(criterion.type(), parameters[ParameterListIndex.TEMPERATURE.ordinal()], noisePoint.temperatureNoise()))
                            return false;
                        break;
                    case WEIRDNESS:
                        if (criterion.checkRange(criterion.type(), parameters[ParameterListIndex.WEIRDNESS.ordinal()], noisePoint.weirdnessNoise()))
                            return false;
                        break;
                }
            }
            return true;
        }

        private static boolean matchesAny(List<SubBiomeMatcher.Criterion> criteria, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
            MultiNoiseUtil.ParameterRange[] parameters = fittestNodes.ultimate().parameters;

            for (SubBiomeMatcher.Criterion criterion : criteria) {
                switch (criterion.target()) {
                    /*
                     * Target a set of criteria
                     */
                    case CRITERIA:
                        if (!criterion.checkCriteria(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise))
                            return true;
                        break;
                    /*
                     * Target biome region
                     */
                    case ALTERNATE:
                        if (!criterion.checkBiome(CriterionTargets.ALTERNATE, fittestNodes, biomePlacement, replacementNoise))
                            return true;
                        break;
                    case CENTER:
                        if (!criterion.checkReplacement(CriterionTargets.CENTER, fittestNodes, noisePoint, replacementRange, replacementNoise))
                            return true;
                        break;
                    case EDGE:
                        if (!criterion.checkReplacement(CriterionTargets.EDGE, fittestNodes, noisePoint, replacementRange, replacementNoise))
                            return true;
                        break;
                    case NEIGHBOR:
                        if (!criterion.checkBiome(CriterionTargets.NEIGHBOR, fittestNodes, biomePlacement, replacementNoise))
                            return true;
                        break;
                    /*
                     * Original biome noise point
                     */
                    case CONTINENTALNESS:
                        if (!criterion.checkRange(criterion.type(), parameters[ParameterListIndex.CONTINENTALNESS.ordinal()], noisePoint.continentalnessNoise()))
                            return true;
                        break;
                    case DEPTH:
                        if (!criterion.checkRange(criterion.type(), parameters[ParameterListIndex.DEPTH.ordinal()], noisePoint.depth()))
                            return true;
                        break;
                    case EROSION:
                        if (!criterion.checkRange(criterion.type(), parameters[ParameterListIndex.EROSION.ordinal()], noisePoint.erosionNoise()))
                            return true;
                        break;
                    case HUMIDITY:
                        if (!criterion.checkRange(criterion.type(), parameters[ParameterListIndex.HUMIDITY.ordinal()], noisePoint.humidityNoise()))
                            return true;
                        break;
                    case ORIGINAL:
                        if (!criterion.checkBiome(CriterionTargets.ORIGINAL, fittestNodes, biomePlacement, replacementNoise))
                            return true;
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

                        if (!criterion.checkRange(criterion.type(), pvRange, pvFromWeirdness(noisePoint.weirdnessNoise())))
                            return true;
                        break;
                    case TEMPERATURE:
                        if (!criterion.checkRange(criterion.type(), parameters[ParameterListIndex.TEMPERATURE.ordinal()], noisePoint.temperatureNoise()))
                            return true;
                        break;
                    case WEIRDNESS:
                        if (!criterion.checkRange(criterion.type(), parameters[ParameterListIndex.WEIRDNESS.ordinal()], noisePoint.weirdnessNoise()))
                            return true;
                        break;
                }
            }
            return false;
        }

        public static long pvFromWeirdness(long weirdness) {
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

        /*
         * NOTE: The astute observer may note the meaning of the invert field seems to be ... inverted.
         *       This is not an accident; the following "check" methods return false for a match because
         *       doing so avoids the requirement to invert all the tests in the table method above.
         *
         *       | invert | result | return | final |
         *       |--------|--------|--------|-------|
         *       | false  | false  | true   | fail  |
         *       | false  | true   | false  | match |
         *       | true   | false  | false  | match |
         *       | true   | true   | true   | fail  |
         */

        public boolean checkBiome(CriterionTargets target, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, float replacementNoise) {
            RegistryEntry<Biome> comparable = null;

            if (target == CriterionTargets.ORIGINAL) {
                comparable = fittestNodes.ultimate().value;
            } else if (target == CriterionTargets.NEIGHBOR) {
                if (fittestNodes.penultimate() != null) {
                    comparable = fittestNodes.penultimate().value;
                }
            } else if (target == CriterionTargets.ALTERNATE) {
                DimensionBiomePlacement.ReplacementRequestSet requests = biomePlacement.replacementRequests.get(secondary);
                DimensionBiomePlacement.ReplacementRequest request = null;
                if (requests != null) {
                    request = requests.selectReplacement(replacementNoise);
                }
                if (request == null || request.biome().equals(DimensionBiomePlacement.VANILLA_PLACEHOLDER)) {
                    // This case requires comparing two keys instead of a key and an entry.
                    return invert == (biome != null && biome.equals(secondary));
                } else {
                    comparable = request.biomeEntry();
                }
            }

            return invert == (comparable != null && (
                    (biome != null && comparable.matchesKey(biome)) ||
                    (biomeTag != null && comparable.isIn(biomeTag))
            ));
        }

        public boolean checkCriteria(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
            boolean result = false;

            if (type == CriterionTypes.ALL_OF) {
                result = matchesAll(criteria, fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise);
            } else if (type == CriterionTypes.ANY_OF) {
                result = matchesAny(criteria, fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise);
            }

            return invert == result;
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
