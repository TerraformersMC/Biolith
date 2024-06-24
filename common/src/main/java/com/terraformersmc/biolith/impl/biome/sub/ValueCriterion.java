package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.sub.BiomeParameterTargets;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Range;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

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
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Range<Float> replacementRange, float replacementNoise) {
        return allowedValues.contains(MultiNoiseUtil.toFloat(parameter.getNoiseValue(noisePoint)));
    }
}
