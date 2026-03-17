package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;

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
    public boolean matches(BiolithFittestNodes<Holder<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, Climate.TargetPoint noisePoint, @Nullable InclusiveRange<Float> replacementRange, float replacementNoise) {
        return !criterion.matches(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise);
    }

    @Override
    public void complete(HolderGetter<Biome> biomeHolderGetter) {
        criterion.complete(biomeHolderGetter);
    }

    @Override
    public void reopen() {
        criterion.reopen();
    }
}
