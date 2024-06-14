package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.subbiome.CriterionType;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public class NeighborCriterion extends AbstractBiomeCriterion {
    public static final MapCodec<NeighborCriterion> CODEC = buildCodec(NeighborCriterion::new);

    public NeighborCriterion(BiomeTarget biomeTarget) {
        super(biomeTarget);
    }

    @Override
    public CriterionType<NeighborCriterion> getType() {
        return BiolithCriterion.NEIGHBOR;
    }

    @Override
    public MapCodec<NeighborCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        if (fittestNodes.penultimate() == null) return false;
        RegistryEntry<Biome> biome = fittestNodes.penultimate().value;
        return biomeTarget.matches(biome);
    }
}
