package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Range;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

public record NotCriterion(Criterion criterion) implements Criterion {
    public static final MapCodec<NotCriterion> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                            Criterion.CODEC.fieldOf("criterion")
                                    .forGetter(NotCriterion::criterion)
                    )
                    .apply(instance, NotCriterion::new));

    @Override
    public CriterionType<NotCriterion> getType() {
        return BiolithCriteria.NOT;
    }

    @Override
    public MapCodec<NotCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Range<Float> replacementRange, float replacementNoise) {
        return !criterion.matches(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise);
    }

    @Override
    public void complete(RegistryEntryLookup<Biome> biomeEntryGetter) {
        criterion.complete(biomeEntryGetter);
    }

    @Override
    public void reopen() {
        criterion.reopen();
    }
}
