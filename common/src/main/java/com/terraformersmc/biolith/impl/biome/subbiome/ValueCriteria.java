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

public class ValueCriteria extends AbstractParameterCriteria {
    public static final MapCodec<ValueCriteria> CODEC = buildCodec(ValueCriteria::new);

    public ValueCriteria(BiomeParameterTarget parameter, float min, float max) {
        super(parameter, min, max);
    }

    @Override
    public CriteriaType<ValueCriteria> getType() {
        return BiolithCriterion.VALUE;
    }

    @Override
    public MapCodec<ValueCriteria> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        float value = parameter.getNoiseValue(noisePoint);
        return allowedValues.contains(value);
    }
}
