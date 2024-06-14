package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.subbiome.Criteria;
import com.terraformersmc.biolith.api.biome.subbiome.CriteriaType;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

import java.util.List;

public record AllOfCriteria(List<Criteria> criterion) implements Criteria {
    public static final MapCodec<AllOfCriteria> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Criteria.CODEC.listOf().fieldOf("criterion").forGetter(AllOfCriteria::criterion)
    ).apply(instance, AllOfCriteria::new));

    @Override
    public CriteriaType<AllOfCriteria> getType() {
        return BiolithCriterion.ALL_OF;
    }

    @Override
    public MapCodec<AllOfCriteria> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        for (Criteria criteria : criterion) {
            if (!criteria.matches(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise)) {
                return false;
            }
        }
        return true;
    }
}
