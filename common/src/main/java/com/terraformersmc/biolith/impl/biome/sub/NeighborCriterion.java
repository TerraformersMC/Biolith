package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;

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
    public boolean matches(BiolithFittestNodes<Holder<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, Climate.TargetPoint noisePoint, @Nullable InclusiveRange<Float> replacementRange, float replacementNoise) {
        if (fittestNodes.penultimate() == null) {
            return false;
        }

        return biomeTarget.matches(fittestNodes.penultimate().value);
    }
}
