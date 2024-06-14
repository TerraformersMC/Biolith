package com.terraformersmc.biolith.api.biome.subbiome;

import com.terraformersmc.biolith.impl.biome.subbiome.*;
import com.terraformersmc.biolith.impl.biome.subbiome.RatioCriteria.RatioTarget;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;

@SuppressWarnings("unused")
public class CriteriaBuilder {
    public static final Criteria NEAR_BORDER = ratioMax(RatioTarget.EDGE, 0.2f);
    public static final Criteria NEAR_INTERIOR = ratioMax(RatioTarget.CENTER, 0.2f);

    public static final Criteria BEACHSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_BEACH));
    public static final Criteria OCEANSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_OCEAN));
    public static final Criteria RIVERSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_RIVER));

    public static Criteria not(Criteria criteria) {
        return new NotCriteria(criteria);
    }

    public static Criteria anyOf(Criteria... criterion) {
        return new AnyOfCriteria(Arrays.stream(criterion).toList());
    }

    public static Criteria allOf(Criteria... criterion) {
        return new AllOfCriteria(Arrays.stream(criterion).toList());
    }

    public static Criteria value(BiomeParameterTarget parameter, float min, float max) {
        return new ValueCriteria(parameter, min, max);
    }

    public static Criteria valueMin(BiomeParameterTarget parameter, float min) {
        return new ValueCriteria(parameter, min, 64);
    }

    public static Criteria valueMax(BiomeParameterTarget parameter, float max) {
        return new ValueCriteria(parameter, -64, max);
    }

    public static Criteria centerDistance(BiomeParameterTarget parameter, float min, float max) {
        return new CenterDistanceCriteria(parameter, min, max);
    }

    public static Criteria centerDistanceMin(BiomeParameterTarget parameter, float min) {
        return new CenterDistanceCriteria(parameter, min, 64);
    }

    public static Criteria centerDistanceMax(BiomeParameterTarget parameter, float max) {
        return new CenterDistanceCriteria(parameter, -64, max);
    }

    public static Criteria ratio(RatioTarget target, float min, float max) {
        return new RatioCriteria(target, min, max);
    }

    public static Criteria ratioMin(RatioTarget target, float min) {
        return new RatioCriteria(target, min, 64);
    }

    public static Criteria ratioMax(RatioTarget target, float max) {
        return new RatioCriteria(target, -64, max);
    }

    public static Criteria original(RegistryKey<Biome> biome) {
        return new OriginalCriteria(AbstractBiomeCriteria.BiomeTarget.ofKey(biome));
    }

    public static Criteria original(TagKey<Biome> tag) {
        return new OriginalCriteria(AbstractBiomeCriteria.BiomeTarget.ofTag(tag));
    }

    public static Criteria neighbor(RegistryKey<Biome> biome) {
        return new NeighborCriteria(AbstractBiomeCriteria.BiomeTarget.ofKey(biome));
    }

    public static Criteria neighbor(TagKey<Biome> tag) {
        return new NeighborCriteria(AbstractBiomeCriteria.BiomeTarget.ofTag(tag));
    }

    public static Criteria alternate(RegistryKey<Biome> biome, RegistryKey<Biome> alternate) {
        return new AlternateCriteria(AbstractBiomeCriteria.BiomeTarget.ofKey(biome), alternate);
    }

    public static Criteria alternate(TagKey<Biome> tag, RegistryKey<Biome> alternate) {
        return new AlternateCriteria(AbstractBiomeCriteria.BiomeTarget.ofTag(tag), alternate);
    }
}
