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

public record NotCriteria(Criteria criteria) implements Criteria {
    public static final MapCodec<NotCriteria> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Criteria.CODEC.fieldOf("criteria").forGetter(NotCriteria::criteria)
    ).apply(instance, NotCriteria::new));

    @Override
    public CriteriaType<NotCriteria> getType() {
        return BiolithCriterion.NOT;
    }

    @Override
    public MapCodec<NotCriteria> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        return !criteria.matches(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise);
    }
}
