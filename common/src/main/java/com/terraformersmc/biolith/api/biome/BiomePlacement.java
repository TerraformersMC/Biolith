package com.terraformersmc.biolith.api.biome;

import com.terraformersmc.biolith.api.biome.sub.Criterion;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

/**
 * Code API methods for biome placement strategies:
 * <ul>
 * <li>{@link #addEnd} - Add End biome by noise</li>
 * <li>{@link #addNether} - Add Nether biome by noise</li>
 * <li>{@link #addOverworld} - Add Overworld biome by noise</li>
 * <li>{@link #removeEnd} - Remove End biome from noise selection</li>
 * <li>{@link #removeNether} - Remove Nether biome from noise selection</li>
 * <li>{@link #removeOverworld} - Remove Overworld biome from noise selection</li>
 * <li>{@link #replaceEnd(ResourceKey, ResourceKey) replaceEnd} - Replace End biome entirely</li>
 * <li>{@link #replaceEnd(ResourceKey, ResourceKey, double) replaceEnd} - Replace portion of End biome</li>
 * <li>{@link #replaceNether(ResourceKey, ResourceKey) replaceNether} - Replace Nether biome entirely</li>
 * <li>{@link #replaceNether(ResourceKey, ResourceKey, double) replaceNether} - Replace portion of Nether biome</li>
 * <li>{@link #replaceOverworld(ResourceKey, ResourceKey) replaceOverworld} - Replace Overworld biome entirely</li>
 * <li>{@link #replaceOverworld(ResourceKey, ResourceKey, double) replaceOverworld} - Replace portion of Overworld biome</li>
 * <li>{@link #addSubEnd} - Replace portion of End biome with sub-biome</li>
 * <li>{@link #addSubNether} - Replace portion of Nether biome with sub-biome</li>
 * <li>{@link #addSubOverworld} - Replace portion of Overworld biome with sub-biome</li>
 * </ul>
 */
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
    public static void addEnd(ResourceKey<Biome> biome, Climate.ParameterPoint noisePoint) {
        BiomeCoordinator.END.addPlacement(biome, noisePoint, false);
    }

    /**
     * Place a Nether biome at the specified mixed noise point.
     *
     * @param biome      The biome to be placed
     * @param noisePoint A multi-noise point at which to place the biome
     */
    public static void addNether(ResourceKey<Biome> biome, Climate.ParameterPoint noisePoint) {
        BiomeCoordinator.NETHER.addPlacement(biome, noisePoint, false);
    }

    /**
     * Place an Overworld biome at the specified mixed noise point.
     *
     * @param biome      The biome to be placed
     * @param noisePoint A multi-noise point at which to place the biome
     */
    public static void addOverworld(ResourceKey<Biome> biome, Climate.ParameterPoint noisePoint) {
        BiomeCoordinator.OVERWORLD.addPlacement(biome, noisePoint, false);
    }


    /**
     * Remove an End biome from all mixed noise points.
     *
     * @param biome The biome to be removed
     */
    public static void removeEnd(ResourceKey<Biome> biome) {
        BiomeCoordinator.END.addRemoval(biome, false);
    }

    /**
     * Remove a Nether biome from all mixed noise points.
     *
     * @param biome The biome to be removed
     */
    public static void removeNether(ResourceKey<Biome> biome) {
        BiomeCoordinator.NETHER.addRemoval(biome, false);
    }

    /**
     * Remove an Overworld biome from all mixed noise points.
     *
     * @param biome The biome to be placed
     */
    public static void removeOverworld(ResourceKey<Biome> biome) {
        BiomeCoordinator.OVERWORLD.addRemoval(biome, false);
    }


    /**
     * Completely replace an End biome with another biome.
     *
     * @param target The biome to be replaced
     * @param biome  The replacement biome
     */
    public static void replaceEnd(ResourceKey<Biome> target, ResourceKey<Biome> biome) {
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
    public static void replaceEnd(ResourceKey<Biome> target, ResourceKey<Biome> biome, double proportion) {
        BiomeCoordinator.END.addReplacement(target, biome, proportion, false);
    }

    /**
     * Completely replace a Nether biome with another biome.
     *
     * @param target The biome to be replaced
     * @param biome  The replacement biome
     */
    public static void replaceNether(ResourceKey<Biome> target, ResourceKey<Biome> biome) {
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
    public static void replaceNether(ResourceKey<Biome> target, ResourceKey<Biome> biome, double proportion) {
        BiomeCoordinator.NETHER.addReplacement(target, biome, proportion, false);
    }

    /**
     * Completely replace an Overworld biome with another biome.
     *
     * @param target The biome to be replaced
     * @param biome  The replacement biome
     */
    public static void replaceOverworld(ResourceKey<Biome> target, ResourceKey<Biome> biome) {
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
    public static void replaceOverworld(ResourceKey<Biome> target, ResourceKey<Biome> biome, double proportion) {
        BiomeCoordinator.OVERWORLD.addReplacement(target, biome, proportion, false);
    }


    /**
     * Add an End biome which replaces matching regions of another biome.  Replacement will occur for any
     * location in the target biome where all conditions of the matcher evaluate a successful match.
     *
     * @param target  The biome to be replaced
     * @param biome   The replacement biome
     * @param criterion Matching {@linkplain Criterion criteria} for when to replace
     */
    public static void addSubEnd(ResourceKey<Biome> target, ResourceKey<Biome> biome, Criterion criterion) {
        BiomeCoordinator.END.addSubBiome(target, biome, criterion, false);
    }

    /**
     * Add a Nether biome which replaces matching regions of another biome.  Replacement will occur for any
     * location in the target biome where all conditions of the matcher evaluate a successful match.
     *
     * @param target  The biome to be replaced
     * @param biome   The replacement biome
     * @param criterion Matching {@linkplain Criterion criteria} for when to replace
     */
    public static void addSubNether(ResourceKey<Biome> target, ResourceKey<Biome> biome, Criterion criterion) {
        BiomeCoordinator.NETHER.addSubBiome(target, biome, criterion, false);
    }

    /**
     * Add an Overworld biome which replaces matching regions of another biome.  Replacement will occur for any
     * location in the target biome where all conditions of the matcher evaluate a successful match.
     *
     * @param target  The biome to be replaced
     * @param biome   The replacement biome
     * @param criterion Matching {@linkplain Criterion criteria} for when to replace
     */
    public static void addSubOverworld(ResourceKey<Biome> target, ResourceKey<Biome> biome, Criterion criterion) {
        BiomeCoordinator.OVERWORLD.addSubBiome(target, biome, criterion, false);
    }
}
