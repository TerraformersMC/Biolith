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

public class OriginalCriterion extends AbstractBiomeCriterion {
    public static final MapCodec<OriginalCriterion> CODEC = buildCodec(OriginalCriterion::new);

    public OriginalCriterion(BiomeTarget biomeTarget) {
        super(biomeTarget);
    }

    @Override
    public CriterionType<OriginalCriterion> getType() {
        return BiolithCriterion.ORIGINAL;
    }

    @Override
    public MapCodec<OriginalCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        RegistryEntry<Biome> biome = fittestNodes.ultimate().value;
        return biomeTarget.matches(biome);
    }
}
