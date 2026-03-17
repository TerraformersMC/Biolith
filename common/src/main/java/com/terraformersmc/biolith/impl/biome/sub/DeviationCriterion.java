package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.api.biome.sub.BiomeParameterTargets;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;

public class DeviationCriterion extends AbstractParameterCriterion {
    public static final MapCodec<DeviationCriterion> CODEC = buildCodec(DeviationCriterion::new);

    public DeviationCriterion(BiomeParameterTargets parameter, float min, float max) {
        super(parameter, min, max);
    }

    @Override
    public CriterionType<DeviationCriterion> getType() {
        return BiolithCriteria.DEVIATION;
    }

    @Override
    public MapCodec<DeviationCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<Holder<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, Climate.TargetPoint noisePoint, @Nullable InclusiveRange<Float> replacementRange, float replacementNoise) {
        long value = parameter.getNoiseValue(noisePoint);
        long parameterCenter = BiomeParameterTargets.parameterCenter(getParameterRange(fittestNodes.ultimate().parameterSpace));

        return allowedValues.isValueInRange(Climate.unquantizeCoord(value - parameterCenter));
    }

    private Climate.Parameter getParameterRange(Climate.Parameter[] parameters) {
        if (parameter == BiomeParameterTargets.PEAKS_VALLEYS) {
            // PV is a calculated noise based on folding weirdness twice
            long weirdnessMin = parameters[BiomeParameterTargets.WEIRDNESS.ordinal()].min();
            long weirdnessMax = parameters[BiomeParameterTargets.WEIRDNESS.ordinal()].max();
            long point1 = BiomeParameterTargets.getPeaksValleysNoiseLong(weirdnessMin);
            long point2 = BiomeParameterTargets.getPeaksValleysNoiseLong(weirdnessMax);
            long pvMin;
            long pvMax;

            // inflection points exist at 1/6 (2/3 of -10k), 1/2 (0), and 5/6 (2/3 of 10k) of weirdness range (-10k to 10k)
            if (weirdnessMin < 0f && weirdnessMax > 0f) {
                pvMin = -10000L;
            } else {
                pvMin = Math.min(point1, point2);
            }
            if ((weirdnessMin < -20000f / 3f && weirdnessMax > -20000f / 3f) || (weirdnessMin < 20000f / 3f && weirdnessMax > 20000f / 3f)) {
                pvMax = 10000L;
            } else {
                pvMax = Math.max(point1, point2);
            }

            return new Climate.Parameter(pvMin, pvMax);
        } else if (parameter.ordinal() < parameters.length) {
            return parameters[parameter.ordinal()];
        }

        throw new IllegalStateException("Unexpected value: " + parameter);
    }
}
