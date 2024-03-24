package com.terraformersmc.biolith.api.biome;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

@SuppressWarnings("unused")
public final class BiomePlacement {
    private BiomePlacement() {
        throw new UnsupportedOperationException();
    }

    /**
     * Place an End biome at the specified mixed noise point.
     *
     * @param biome      The biome to be placed
     * @param noisePoint A multi-noise point at which to place the biome
     */
    public static void addEnd(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube noisePoint) {
        BiomeCoordinator.END.addPlacement(biome, noisePoint, false);
    }

    /**
     * Place a Nether biome at the specified mixed noise point.
     *
     * @param biome      The biome to be placed
     * @param noisePoint A multi-noise point at which to place the biome
     */
    public static void addNether(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube noisePoint) {
        BiomeCoordinator.NETHER.addPlacement(biome, noisePoint, false);
    }

    /**
     * Place an Overworld biome at the specified mixed noise point.
     *
     * @param biome      The biome to be placed
     * @param noisePoint A multi-noise point at which to place the biome
     */
    public static void addOverworld(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube noisePoint) {
        BiomeCoordinator.OVERWORLD.addPlacement(biome, noisePoint, false);
    }


    /**
     * Remove an End biome from all mixed noise points.
     *
     * @param biome The biome to be removed
     */
    public static void removeEnd(RegistryKey<Biome> biome) {
        BiomeCoordinator.END.addRemoval(biome, false);
    }

    /**
     * Remove a Nether biome from all mixed noise points.
     *
     * @param biome The biome to be removed
     */
    public static void removeNether(RegistryKey<Biome> biome) {
        BiomeCoordinator.NETHER.addRemoval(biome, false);
    }

    /**
     * Remove an Overworld biome from all mixed noise points.
     *
     * @param biome The biome to be placed
     */
    public static void removeOverworld(RegistryKey<Biome> biome) {
        BiomeCoordinator.OVERWORLD.addRemoval(biome, false);
    }


    /**
     * Completely replace an End biome with another biome.
     *
     * @param target The biome to be replaced
     * @param biome  The replacement biome
     */
    public static void replaceEnd(RegistryKey<Biome> target, RegistryKey<Biome> biome) {
        BiomeCoordinator.END.addReplacement(target, biome, 1.0D, false);
    }

    /**
     * Partially replace an End biome with another biome.  The proportion must be a positive number,
     * and any number above 1.0d will result in complete replacement.
     *
     * @param target     The biome to be replaced
     * @param biome      The replacement biome
     * @param proportion Approximate fraction of the target biome's volume to replace
     */
    public static void replaceEnd(RegistryKey<Biome> target, RegistryKey<Biome> biome, double proportion) {
        BiomeCoordinator.END.addReplacement(target, biome, proportion, false);
    }

    /**
     * Completely replace a Nether biome with another biome.
     *
     * @param target The biome to be replaced
     * @param biome  The replacement biome
     */
    public static void replaceNether(RegistryKey<Biome> target, RegistryKey<Biome> biome) {
        BiomeCoordinator.NETHER.addReplacement(target, biome, 1.0D, false);
    }

    /**
     * Partially replace a Nether biome with another biome.  The proportion must be a positive number,
     * and any number above 1.0d will result in complete replacement.
     *
     * @param target     The biome to be replaced
     * @param biome      The replacement biome
     * @param proportion Approximate fraction of the target biome's volume to replace
     */
    public static void replaceNether(RegistryKey<Biome> target, RegistryKey<Biome> biome, double proportion) {
        BiomeCoordinator.NETHER.addReplacement(target, biome, proportion, false);
    }

    /**
     * Completely replace an Overworld biome with another biome.
     *
     * @param target The biome to be replaced
     * @param biome  The replacement biome
     */
    public static void replaceOverworld(RegistryKey<Biome> target, RegistryKey<Biome> biome) {
        BiomeCoordinator.OVERWORLD.addReplacement(target, biome, 1.0D, false);
    }

    /**
     * Partially replace an Overworld biome with another biome.  The proportion must be a positive number,
     * and any number above 1.0d will result in complete replacement.
     *
     * @param target     The biome to be replaced
     * @param biome      The replacement biome
     * @param proportion Approximate fraction of the target biome's volume to replace
     */
    public static void replaceOverworld(RegistryKey<Biome> target, RegistryKey<Biome> biome, double proportion) {
        BiomeCoordinator.OVERWORLD.addReplacement(target, biome, proportion, false);
    }


    /**
     * Add an End biome which replaces matching regions of another biome.  Replacement will occur for any
     * location in the target biome where all conditions of the matcher evaluate a successful match.
     *
     * @param target  The biome to be replaced
     * @param biome   The replacement biome
     * @param matcher Matching criteria for when to replace
     */
    public static void addSubEnd(RegistryKey<Biome> target, RegistryKey<Biome> biome, SubBiomeMatcher matcher) {
        BiomeCoordinator.END.addSubBiome(target, biome, matcher, false);
    }

    /**
     * Add a Nether biome which replaces matching regions of another biome.  Replacement will occur for any
     * location in the target biome where all conditions of the matcher evaluate a successful match.
     *
     * @param target  The biome to be replaced
     * @param biome   The replacement biome
     * @param matcher Matching criteria for when to replace
     */
    public static void addSubNether(RegistryKey<Biome> target, RegistryKey<Biome> biome, SubBiomeMatcher matcher) {
        BiomeCoordinator.NETHER.addSubBiome(target, biome, matcher, false);
    }

    /**
     * Add an Overworld biome which replaces matching regions of another biome.  Replacement will occur for any
     * location in the target biome where all conditions of the matcher evaluate a successful match.
     *
     * @param target  The biome to be replaced
     * @param biome   The replacement biome
     * @param matcher Matching criteria for when to replace
     */
    public static void addSubOverworld(RegistryKey<Biome> target, RegistryKey<Biome> biome, SubBiomeMatcher matcher) {
        BiomeCoordinator.OVERWORLD.addSubBiome(target, biome, matcher, false);
    }
}
