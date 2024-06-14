package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.subbiome.Criteria;
import com.terraformersmc.biolith.api.biome.subbiome.CriteriaType;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public record RatioCriteria(RatioTarget target, Range<Float> allowedValues) implements Criteria {
    public static final MapCodec<RatioCriteria> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        RatioTarget.CODEC.fieldOf("target").forGetter(RatioCriteria::target),
        Codec.FLOAT.optionalFieldOf("min", Float.MIN_VALUE).forGetter(RatioCriteria::min),
        Codec.FLOAT.optionalFieldOf("max", Float.MAX_VALUE).forGetter(RatioCriteria::max)
    ).apply(instance, RatioCriteria::new));

    public RatioCriteria(RatioTarget target, float min, float max) {
        this(target, new Range<>(min, max));
    }

    public float min() {
        return allowedValues.minInclusive();
    }

    public float max() {
        return allowedValues.maxInclusive();
    }
    @Override
    public CriteriaType<? extends Criteria> getType() {
        return BiolithCriterion.RATIO;
    }

    @Override
    public MapCodec<? extends Criteria> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        float comparable = Float.MAX_VALUE;

        if (target == RatioTarget.CENTER) {
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
        } else {
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

        return allowedValues.contains(comparable);
    }

    private static long parameterCenter(MultiNoiseUtil.ParameterRange range) {
        return (range.min() + range.max()) / 2L;
    }

    private static MultiNoiseUtil.NoiseValuePoint parametersCenterPoint(MultiNoiseUtil.ParameterRange[] parameters) {
        return new MultiNoiseUtil.NoiseValuePoint(
            parameterCenter(parameters[0]),
            parameterCenter(parameters[1]),
            parameterCenter(parameters[2]),
            parameterCenter(parameters[3]),
            parameterCenter(parameters[4]),
            parameterCenter(parameters[5])
        );
    }

    private static long getSquaredDistance(MultiNoiseUtil.NoiseValuePoint point1, MultiNoiseUtil.NoiseValuePoint point2) {
        return  MathHelper.square(point1.temperatureNoise() - point2.temperatureNoise()) +
                MathHelper.square(point1.humidityNoise() - point2.humidityNoise()) +
                MathHelper.square(point1.continentalnessNoise() - point2.continentalnessNoise()) +
                MathHelper.square(point1.erosionNoise() - point2.erosionNoise()) +
                MathHelper.square(point1.depth() - point2.depth()) +
                MathHelper.square(point1.weirdnessNoise() - point2.weirdnessNoise());
    };

    public enum RatioTarget implements StringIdentifiable {
        CENTER("center"),
        EDGE("edge");

        private static final Codec<RatioTarget> CODEC = StringIdentifiable.createCodec(RatioTarget::values);

        private final String name;

        RatioTarget(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}
