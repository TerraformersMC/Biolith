package com.terraformersmc.biolith.api.biome;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

@SuppressWarnings("unused")
public class BiomePlacement {
    /**
     * BiomePlacement.replaceNether()
     *
     * Completely replace a Nether biome with another biome.
     *
     * @param target RegistryKey<Biome> - The biome to be replaced
     * @param biome RegistryKey<Biome> - The replacement biome
     */
    public static void replaceNether(RegistryKey<Biome> target, RegistryKey<Biome> biome) {
        BiomeCoordinator.NETHER.addReplacement(target, biome, 1.0D);
    }

    /**
     * BiomePlacement.replaceNether()
     *
     * Partially replace a Nether biome with another biome.  The proportion must be a positive number,
     * and any number above 1.0d will result in complete replacement.
     *
     * @param target RegistryKey<Biome> - The biome to be replaced
     * @param biome RegistryKey<Biome> - The replacement biome
     * @param proportion double - Approximate fraction of the target biome's volume to replace
     */
    public static void replaceNether(RegistryKey<Biome> target, RegistryKey<Biome> biome, double proportion) {
        BiomeCoordinator.NETHER.addReplacement(target, biome, proportion);
    }

    /**
     * BiomePlacement.replaceOverworld()
     *
     * Completely replace an Overworld biome with another biome.
     *
     * @param target RegistryKey<Biome> - The biome to be replaced
     * @param biome RegistryKey<Biome> - The replacement biome
     */
    public static void replaceOverworld(RegistryKey<Biome> target, RegistryKey<Biome> biome) {
        BiomeCoordinator.OVERWORLD.addReplacement(target, biome, 1.0D);
    }

    /**
     * BiomePlacement.replaceOverworld()
     *
     * Partially replace an Overworld biome with another biome.  The proportion must be a positive number,
     * and any number above 1.0d will result in complete replacement.
     *
     * @param target RegistryKey<Biome> - The biome to be replaced
     * @param biome RegistryKey<Biome> - The replacement biome
     * @param proportion double - Approximate fraction of the target biome's volume to replace
     */
    public static void replaceOverworld(RegistryKey<Biome> target, RegistryKey<Biome> biome, double proportion) {
        BiomeCoordinator.OVERWORLD.addReplacement(target, biome, proportion);
    }


    /**
     * BiomePlacement.addSubNether()
     *
     * Add a Nether biome which replaces matching regions of another biome.  Replacement will occur for any
     * location in the target biome where all conditions of the matcher evaluate a successful match.
     *
     * @param target RegistryKey<Biome> - The biome to be replaced
     * @param biome RegistryKey<Biome> - The replacement biome
     * @param matcher SubBiomeMatcher - Matching criteria for when to replace
     */
    public static void addSubNether(RegistryKey<Biome> target, RegistryKey<Biome> biome, SubBiomeMatcher matcher) {
        BiomeCoordinator.NETHER.addSubBiome(target, biome, matcher);
    }

    /**
     * BiomePlacement.addSubOverworld()
     *
     * Add an Overworld biome which replaces matching regions of another biome.  Replacement will occur for any
     * location in the target biome where all conditions of the matcher evaluate a successful match.
     *
     * @param target RegistryKey<Biome> - The biome to be replaced
     * @param biome RegistryKey<Biome> - The replacement biome
     * @param matcher SubBiomeMatcher - Matching criteria for when to replace
     */
    public static void addSubOverworld(RegistryKey<Biome> target, RegistryKey<Biome> biome, SubBiomeMatcher matcher) {
        BiomeCoordinator.OVERWORLD.addSubBiome(target, biome, matcher);
    }
}
