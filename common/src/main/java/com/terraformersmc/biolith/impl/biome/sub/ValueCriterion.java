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

public class ValueCriterion extends AbstractParameterCriterion {
    public static final MapCodec<ValueCriterion> CODEC = buildCodec(ValueCriterion::new);

    public ValueCriterion(BiomeParameterTargets parameter, float min, float max) {
        super(parameter, min, max);
    }

    @Override
    public CriterionType<ValueCriterion> getType() {
        return BiolithCriteria.VALUE;
    }

    @Override
    public MapCodec<ValueCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<Holder<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, Climate.TargetPoint noisePoint, @Nullable InclusiveRange<Float> replacementRange, float replacementNoise) {
        return allowedValues.isValueInRange(Climate.unquantizeCoord(parameter.getNoiseValue(noisePoint)));
    }
}
