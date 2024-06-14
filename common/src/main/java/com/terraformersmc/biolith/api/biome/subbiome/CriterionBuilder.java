package com.terraformersmc.biolith.api.biome.subbiome;

import com.terraformersmc.biolith.impl.biome.subbiome.*;
import com.terraformersmc.biolith.impl.biome.subbiome.RatioCriterion.RatioTarget;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;

@SuppressWarnings("unused")
public class CriterionBuilder {
    public static final Criterion NEAR_BORDER = ratioMax(RatioTarget.EDGE, 0.2f);
    public static final Criterion NEAR_INTERIOR = ratioMax(RatioTarget.CENTER, 0.2f);

    public static final Criterion BEACHSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_BEACH));
    public static final Criterion OCEANSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_OCEAN));
    public static final Criterion RIVERSIDE = allOf(NEAR_BORDER, neighbor(BiomeTags.IS_RIVER));

    public static Criterion not(Criterion criterion) {
        return new NotCriterion(criterion);
    }

    public static Criterion anyOf(Criterion... criterion) {
        return new AnyOfCriterion(Arrays.stream(criterion).toList());
    }

    public static Criterion allOf(Criterion... criterion) {
        return new AllOfCriterion(Arrays.stream(criterion).toList());
    }

    public static Criterion value(BiomeParameterTarget parameter, float min, float max) {
        return new ValueCriterion(parameter, min, max);
    }

    public static Criterion valueMin(BiomeParameterTarget parameter, float min) {
        return new ValueCriterion(parameter, min, 64);
    }

    public static Criterion valueMax(BiomeParameterTarget parameter, float max) {
        return new ValueCriterion(parameter, -64, max);
    }

    public static Criterion centerDistance(BiomeParameterTarget parameter, float min, float max) {
        return new CenterDistanceCriterion(parameter, min, max);
    }

    public static Criterion centerDistanceMin(BiomeParameterTarget parameter, float min) {
        return new CenterDistanceCriterion(parameter, min, 64);
    }

    public static Criterion centerDistanceMax(BiomeParameterTarget parameter, float max) {
        return new CenterDistanceCriterion(parameter, -64, max);
    }

    public static Criterion ratio(RatioTarget target, float min, float max) {
        return new RatioCriterion(target, min, max);
    }

    public static Criterion ratioMin(RatioTarget target, float min) {
        return new RatioCriterion(target, min, 64);
    }

    public static Criterion ratioMax(RatioTarget target, float max) {
        return new RatioCriterion(target, -64, max);
    }

    public static Criterion original(RegistryKey<Biome> biome) {
        return new OriginalCriterion(AbstractBiomeCriterion.BiomeTarget.ofKey(biome));
    }

    public static Criterion original(TagKey<Biome> tag) {
        return new OriginalCriterion(AbstractBiomeCriterion.BiomeTarget.ofTag(tag));
    }

    public static Criterion neighbor(RegistryKey<Biome> biome) {
        return new NeighborCriterion(AbstractBiomeCriterion.BiomeTarget.ofKey(biome));
    }

    public static Criterion neighbor(TagKey<Biome> tag) {
        return new NeighborCriterion(AbstractBiomeCriterion.BiomeTarget.ofTag(tag));
    }

    public static Criterion alternate(RegistryKey<Biome> biome, RegistryKey<Biome> alternate) {
        return new AlternateCriterion(AbstractBiomeCriterion.BiomeTarget.ofKey(biome), alternate);
    }

    public static Criterion alternate(TagKey<Biome> tag, RegistryKey<Biome> alternate) {
        return new AlternateCriterion(AbstractBiomeCriterion.BiomeTarget.ofTag(tag), alternate);
    }
}
