package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.subbiome.Criterion;
import com.terraformersmc.biolith.api.biome.subbiome.CriterionType;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public record NotCriterion(Criterion criterion) implements Criterion {
    public static final MapCodec<NotCriterion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Criterion.CODEC.fieldOf("criterion").forGetter(NotCriterion::criterion)
    ).apply(instance, NotCriterion::new));

    @Override
    public CriterionType<NotCriterion> getType() {
        return BiolithCriterion.NOT;
    }

    @Override
    public MapCodec<NotCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        return !criterion.matches(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise);
    }
}
