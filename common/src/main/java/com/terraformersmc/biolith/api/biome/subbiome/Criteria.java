package com.terraformersmc.biolith.api.biome.subbiome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import com.terraformersmc.biolith.impl.biome.subbiome.AllOfCriteria;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public interface Criteria {
    Codec<Criteria> CODEC = CriteriaType.TYPE_CODEC.dispatch("type", Criteria::getType, CriteriaType::getCodec);
    Codec<Criteria> MATCHER_CODEC = Codec.withAlternative(CODEC, CODEC.listOf(), AllOfCriteria::new);

    CriteriaType<? extends Criteria> getType();

    MapCodec<? extends Criteria> getCodec();

    boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise);
}
