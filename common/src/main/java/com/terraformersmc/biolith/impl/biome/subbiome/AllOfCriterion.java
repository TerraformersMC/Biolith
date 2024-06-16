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

import java.util.List;

public record AllOfCriterion(List<Criterion> criteria) implements Criterion {
    public static final MapCodec<AllOfCriterion> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Criterion.CODEC.listOf().fieldOf("criteria").forGetter(AllOfCriterion::criteria)
    ).apply(instance, AllOfCriterion::new));

    @Override
    public CriterionType<AllOfCriterion> getType() {
        return BiolithCriterion.ALL_OF;
    }

    @Override
    public MapCodec<AllOfCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        for (Criterion criterion : this.criteria) {
            if (!criterion.matches(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise)) {
                return false;
            }
        }
        return true;
    }
}
