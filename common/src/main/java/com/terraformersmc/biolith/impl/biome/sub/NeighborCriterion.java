package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Range;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

public class NeighborCriterion extends AbstractBiomeCriterion {
    public static final MapCodec<NeighborCriterion> CODEC = buildCodec(NeighborCriterion::new);

    public NeighborCriterion(BiomeTarget biomeTarget) {
        super(biomeTarget);
    }

    @Override
    public CriterionType<NeighborCriterion> getType() {
        return BiolithCriteria.NEIGHBOR;
    }

    @Override
    public MapCodec<NeighborCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Range<Float> replacementRange, float replacementNoise) {
        if (fittestNodes.penultimate() == null) {
            return false;
        }

        return biomeTarget.matches(fittestNodes.penultimate().value);
    }
}
