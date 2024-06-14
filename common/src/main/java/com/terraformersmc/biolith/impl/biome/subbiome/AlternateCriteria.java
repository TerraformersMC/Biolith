package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.subbiome.CriteriaType;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

public class AlternateCriteria extends AbstractBiomeCriteria {
    public static final MapCodec<AlternateCriteria> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        BiomeTarget.CODEC.fieldOf("biome_target").forGetter(AbstractBiomeCriteria::biomeTarget),
        RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("alternate").forGetter(AlternateCriteria::alternate)
    ).apply(instance, AlternateCriteria::new));

    private final RegistryKey<Biome> alternate;

    public AlternateCriteria(BiomeTarget biomeTarget, RegistryKey<Biome> alternate) {
        super(biomeTarget);
        this.alternate = alternate;
    }

    @Override
    public CriteriaType<AlternateCriteria> getType() {
        return BiolithCriterion.ALTERNATE;
    }

    public RegistryKey<Biome> alternate() {
        return alternate;
    }

    @Override
    public MapCodec<AlternateCriteria> getCodec() {
        return CODEC;
    }

    @Override
    public boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc replacementRange, float replacementNoise) {
        DimensionBiomePlacement.ReplacementRequestSet requests = biomePlacement.getReplacementRequest(alternate);
        DimensionBiomePlacement.ReplacementRequest request = null;
        if (requests != null) {
            request = requests.selectReplacement(replacementNoise);
        }
        if (request == null || request.biome().equals(DimensionBiomePlacement.VANILLA_PLACEHOLDER)) {
            return biomeTarget.key().map(key -> key.equals(alternate)).orElse(false);
        } else {
            return biomeTarget.matches(request.biomeEntry());
        }
    }
}
