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
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Range<Float> replacementRange, float replacementNoise) {
        return biomeTarget.matches(fittestNodes.ultimate().value);
    }
}
