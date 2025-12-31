package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

public record AllOfCriterion(List<Criterion> criteria) implements Criterion {
    public static final MapCodec<AllOfCriterion> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                            Criterion.CODEC.listOf().fieldOf("criteria")
                                    .forGetter(AllOfCriterion::criteria)
                    )
                    .apply(instance, AllOfCriterion::new));

    @Override
    public CriterionType<AllOfCriterion> getType() {
        return BiolithCriteria.ALL_OF;
    }

    @Override
    public MapCodec<AllOfCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<Holder<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, Climate.TargetPoint noisePoint, @Nullable InclusiveRange<Float> replacementRange, float replacementNoise) {
        for (Criterion criterion : this.criteria) {
            if (!criterion.matches(fittestNodes, biomePlacement, noisePoint, replacementRange, replacementNoise)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void complete(HolderGetter<Biome> biomeEntryGetter) {
        criteria.forEach(criterion -> criterion.complete(biomeEntryGetter));
    }

    @Override
    public void reopen() {
        criteria.forEach(Criterion::reopen);
    }
}
