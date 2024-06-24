package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Range;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

public class AlternateCriterion extends AbstractBiomeCriterion {
    public static final MapCodec<AlternateCriterion> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                            BiomeTarget.CODEC.fieldOf("biome")
                                    .forGetter(AbstractBiomeCriterion::biomeTarget),
                            RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("alternate")
                                    .forGetter(AlternateCriterion::alternate)
                    )
                    .apply(instance, AlternateCriterion::new));

    private final RegistryKey<Biome> alternate;
    private RegistryEntry<Biome> alternateEntry;

    public AlternateCriterion(BiomeTarget biomeTarget, RegistryKey<Biome> alternate) {
        super(biomeTarget);
        this.alternate = alternate;
    }

    public RegistryKey<Biome> alternate() {
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
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Range<Float> replacementRange, float replacementNoise) {
        Pair<RegistryKey<Biome>, RegistryEntry<Biome>> replacement = biomePlacement.getReplacementPair(alternate, replacementNoise);

        if (replacement == null || replacement.getFirst().equals(DimensionBiomePlacement.VANILLA_PLACEHOLDER)) {
            return biomeTarget.matches(alternateEntry);
        } else {
            return biomeTarget.matches(replacement.getSecond());
        }
    }

    @Override
    public void complete(RegistryEntryLookup<Biome> biomeEntryGetter) {
        alternateEntry = biomeEntryGetter.getOrThrow(alternate);
    }

    @Override
    public void reopen() {
        alternateEntry = null;
    }
}
