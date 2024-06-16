package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.subbiome.BiomeParameterTarget;
import com.terraformersmc.biolith.api.biome.subbiome.CriterionType;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public class ValueCriterion extends AbstractParameterCriterion {
    public static final MapCodec<ValueCriterion> CODEC = buildCodec(ValueCriterion::new);

    public ValueCriterion(BiomeParameterTarget parameter, float min, float max) {
        super(parameter, min, max);
    }

    @Override
    public CriterionType<ValueCriterion> getType() {
        return BiolithCriterion.VALUE;
    }

    @Override
    public MapCodec<ValueCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        float value = parameter.getNoiseValue(noisePoint);
        return allowedValues.contains(value);
    }
}
