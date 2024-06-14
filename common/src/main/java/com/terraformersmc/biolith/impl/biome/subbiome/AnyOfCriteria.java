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

public record AnyOfCriteria(List<Criteria> criterion) implements Criteria {
    public static final MapCodec<AnyOfCriteria> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Criteria.CODEC.listOf().fieldOf("criterion").forGetter(AnyOfCriteria::criterion)
    ).apply(instance, AnyOfCriteria::new));

    @Override
    public CriteriaType<AnyOfCriteria> getType() {
        return BiolithCriterion.ANY_OF;
    }

    @Override
    public MapCodec<AnyOfCriteria> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        for (Criteria criteria : criterion) {
            if (!criteria.matches(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise)) {
                return true;
            }
        }
        return false;
    }
}
