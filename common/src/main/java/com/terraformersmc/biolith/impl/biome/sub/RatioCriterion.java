package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.sub.BiomeParameterTargets;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.api.biome.sub.RatioTargets;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

public record RatioCriterion(RatioTargets target, Range<Float> allowedValues) implements Criterion {
    public static final MapCodec<RatioCriterion> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                            RatioTargets.CODEC.fieldOf("target")
                                    .forGetter(RatioCriterion::target),
                            Codec.FLOAT.optionalFieldOf("min", Float.NEGATIVE_INFINITY)
                                    .forGetter(RatioCriterion::min),
                            Codec.FLOAT.optionalFieldOf("max", Float.POSITIVE_INFINITY)
                                    .forGetter(RatioCriterion::max)
                    )
                    .apply(instance, RatioCriterion::new));

    public RatioCriterion(RatioTargets target, float min, float max) {
        this(target, new Range<>(min, max));
    }

    public float min() {
        return allowedValues.minInclusive();
    }

    public float max() {
        return allowedValues.maxInclusive();
    }
    @Override
    public CriterionType<? extends Criterion> getType() {
        return BiolithCriteria.RATIO;
    }

    @Override
    public MapCodec<? extends Criterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Range<Float> replacementRange, float replacementNoise) {
        float comparable;

        if (target == RatioTargets.CENTER) {
            // Vanilla biomes pre-Biolith replacement; /10k is analogous to MultiNoiseUtil.toFloat(); param 6 is offset
            comparable = MathHelper.sqrt((float) BiomeParameterTargets.getSquaredDistance(
                    BiomeParameterTargets.parametersCenterPoint(fittestNodes.ultimate().parameters),
                    noisePoint, fittestNodes.ultimate().parameters[6].min())) / 10000f;
            // Post-replacement we need to add in the replacement noise restriction
            if (replacementRange != null) {
                // Replacement noise at the ends of the spectrum have centers at their extremities; thus the crap.
                if (replacementRange.minInclusive() <= 0f) {
                    if (replacementRange.maxInclusive() < 1f) {
                        comparable = Math.max(replacementNoise, comparable);
                    }
                } else if (replacementRange.maxInclusive() >= 1f) {
                    comparable = Math.max(1f - replacementNoise, comparable);
                } else {
                    comparable = Math.max(Math.abs(replacementNoise -
                            (replacementRange.minInclusive() + replacementRange.maxInclusive()) / 2f), comparable);
                }
            }
        } else if (target == RatioTargets.EDGE) {
            // Vanilla biomes pre-Biolith replacement
            if (fittestNodes.penultimate() == null) {
                comparable = 1f;
            } else if (fittestNodes.penultimateDistance() == 0) {
                comparable = 0f;
            } else {
                comparable = (float) (fittestNodes.penultimateDistance() - fittestNodes.ultimateDistance()) /
                             (float) fittestNodes.penultimateDistance();
            }
            // Post-replacement
            if (replacementRange != null) {
                // Replacement noise at the ends of the spectrum have only one edge; thus the crap.
                if (replacementRange.minInclusive() <= 0f) {
                    if (replacementRange.maxInclusive() < 1f) {
                        comparable = Math.min(replacementRange.maxInclusive() - replacementNoise, comparable);
                    }
                } else if (replacementRange.maxInclusive() >= 1f) {
                    comparable = Math.min(replacementNoise - replacementRange.minInclusive(), comparable);
                } else {
                    comparable = Math.min(Math.min(replacementNoise - replacementRange.minInclusive(),
                            replacementRange.maxInclusive() - replacementNoise), comparable);
                }
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + target);
        }

        return allowedValues.contains(comparable);
    }
}
