package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;

public class AlternateCriterion extends AbstractBiomeCriterion {
    public static final MapCodec<AlternateCriterion> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                            BiomeTarget.CODEC.fieldOf("biome")
                                    .forGetter(AbstractBiomeCriterion::biomeTarget),
                            ResourceKey.codec(Registries.BIOME).fieldOf("alternate")
                                    .forGetter(AlternateCriterion::alternate)
                    )
                    .apply(instance, AlternateCriterion::new));

    private final ResourceKey<Biome> alternate;
    private @Nullable Holder<Biome> alternateEntry;

    public AlternateCriterion(BiomeTarget biomeTarget, ResourceKey<Biome> alternate) {
        super(biomeTarget);
        this.alternate = alternate;
    }

    public ResourceKey<Biome> alternate() {
        return alternate;
    }

    @Override
    public CriterionType<AlternateCriterion> getType() {
        return BiolithCriteria.ALTERNATE;
    }

    @Override
    public MapCodec<AlternateCriterion> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<Holder<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, Climate.TargetPoint noisePoint, @Nullable InclusiveRange<Float> replacementRange, float replacementNoise) {
        Pair<ResourceKey<Biome>, Holder<Biome>> replacement = biomePlacement.getReplacementPair(alternate, replacementNoise);

        if (replacement == null || replacement.getFirst().equals(DimensionBiomePlacement.VANILLA_PLACEHOLDER)) {
            return biomeTarget.matches(alternateEntry);
        } else {
            return biomeTarget.matches(replacement.getSecond());
        }
    }

    @Override
    public void complete(HolderGetter<Biome> biomeHolderGetter) {
        alternateEntry = biomeHolderGetter.getOrThrow(alternate);
    }

    @Override
    public void reopen() {
        alternateEntry = null;
    }
}
