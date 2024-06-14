package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.subbiome.CriteriaType;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public class OriginalCriteria extends AbstractBiomeCriteria {
    public static final MapCodec<OriginalCriteria> CODEC = buildCodec(OriginalCriteria::new);

    public OriginalCriteria(BiomeTarget biomeTarget) {
        super(biomeTarget);
    }

    @Override
    public CriteriaType<OriginalCriteria> getType() {
        return BiolithCriterion.ORIGINAL;
    }

    @Override
    public MapCodec<OriginalCriteria> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        RegistryEntry<Biome> biome = fittestNodes.ultimate().value;
        return biomeTarget.matches(biome);
    }
}
