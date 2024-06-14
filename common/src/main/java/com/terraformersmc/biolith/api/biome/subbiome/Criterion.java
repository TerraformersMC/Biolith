package com.terraformersmc.biolith.api.biome.subbiome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import com.terraformersmc.biolith.impl.biome.subbiome.AllOfCriterion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public interface Criterion {
    Codec<Criterion> CODEC = CriterionType.TYPE_CODEC.dispatch("type", Criterion::getType, CriterionType::getCodec);
    Codec<Criterion> MATCHER_CODEC = Codec.withAlternative(CODEC, CODEC.listOf(), AllOfCriterion::new);

    CriterionType<? extends Criterion> getType();

    MapCodec<? extends Criterion> getCodec();

    boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise);
}
