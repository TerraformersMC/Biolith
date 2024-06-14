package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.subbiome.BiomeParameterTarget;
import com.terraformersmc.biolith.api.biome.subbiome.CriteriaType;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public class CenterDistanceCriteria extends AbstractParameterCriteria {
    public static final MapCodec<CenterDistanceCriteria> CODEC = buildCodec(CenterDistanceCriteria::new);

    public CenterDistanceCriteria(BiomeParameterTarget parameter, float min, float max) {
        super(parameter, min, max);
    }

    @Override
    public CriteriaType<CenterDistanceCriteria> getType() {
        return BiolithCriterion.CENTER_DISTANCE;
    }

    @Override
    public MapCodec<CenterDistanceCriteria> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        float value = parameter.getNoiseValue(noisePoint);
        float parameterCenter = parameterCenter(getParameterRange(fittestNodes.ultimate().parameters));

        return allowedValues.contains(value - parameterCenter);
    }

    private MultiNoiseUtil.ParameterRange getParameterRange(MultiNoiseUtil.ParameterRange[] allParameters) {
        if (parameter == BiomeParameterTarget.PEAKS_VALLEYS) {
            long weirdnessMin = allParameters[6].min();
            long weirdnessMax = allParameters[6].max();
            long point1 = BiomeParameterTarget.getPV(weirdnessMin);
            long point2 = BiomeParameterTarget.getPV(weirdnessMax);
            long pvMin;
            long pvMax;

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

           return new MultiNoiseUtil.ParameterRange(pvMin, pvMax);
        } else {
            return allParameters[switch (parameter) {
                case TEMPERATURE -> 1;
                case HUMIDITY -> 2;
                case CONTINENTALNESS -> 3;
                case EROSION -> 4;
                case DEPTH -> 5;
                case WEIRDNESS -> 6;
                default -> throw new IllegalStateException("Unexpected value: " + parameter);
            }];
        }
    }

    private static long parameterCenter(MultiNoiseUtil.ParameterRange range) {
        return (range.min() + range.max()) / 2L;
    }
}
