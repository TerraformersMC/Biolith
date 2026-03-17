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

public class OriginalCriterion extends AbstractBiomeCriterion {
    public static final MapCodec<OriginalCriterion> CODEC = buildCodec(OriginalCriterion::new);

    public OriginalCriterion(BiomeTarget biomeTarget) {
        super(biomeTarget);
    }

    @Override
    public CriterionType<OriginalCriterion> getType() {
        return BiolithCriteria.ORIGINAL;
    }

    @Override
    public MapCodec<OriginalCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<Holder<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, Climate.TargetPoint noisePoint, @Nullable InclusiveRange<Float> replacementRange, float replacementNoise) {
        return biomeTarget.matches(fittestNodes.ultimate().value);
    }
}
