package com.terraformersmc.biolith.api.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * <p>
 * This class is deprecated in Biolith 3 and will be removed in Biolith 4.  Please
 * switch to {@linkplain ModernerBetaBiomeKeys} to access all available biome keys.
 * </p><p>
 * Copies of the registry keys of Modern Beta's biomes.  These keys can be used to
 * target Modern Beta biomes with Biolith's replacement noise, without depending on
 * Modern Beta when building.  Modern Beta biomes can be replaced, but they cannot
 * be placed or targeted with sub-biome matchers.
 * </p><p>
 * For example, to replace 20% of "Beta Forest" with "Forest A" and 20% with "Forest B":
 * </p>
 * <pre>{@code
 * if (BiolithCompats.isCompatEnabled(BiolithCompats.MODERN_BETA)) {
 *     BiomePlacement.replaceOverworld(ModernBetaBiomeKeys.BETA_FOREST, FOREST_A, 0.4D);
 *     BiomePlacement.replaceOverworld(ModernBetaBiomeKeys.BETA_FOREST, FOREST_B, 0.4D);
 * }
 * }</pre>
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("unused")
public class ModernBetaBiomeKeys {
    public static final ResourceKey<Biome> BETA_FOREST = keyOf("beta_forest");
    public static final ResourceKey<Biome> BETA_SHRUBLAND = keyOf("beta_shrubland");
    public static final ResourceKey<Biome> BETA_DESERT = keyOf("beta_desert");
    public static final ResourceKey<Biome> BETA_SAVANNA = keyOf("beta_savanna");
    public static final ResourceKey<Biome> BETA_PLAINS = keyOf("beta_plains");
    public static final ResourceKey<Biome> BETA_SEASONAL_FOREST = keyOf("beta_seasonal_forest");
    public static final ResourceKey<Biome> BETA_RAINFOREST = keyOf("beta_rainforest");
    public static final ResourceKey<Biome> BETA_SWAMPLAND = keyOf("beta_swampland");
    public static final ResourceKey<Biome> BETA_TAIGA = keyOf("beta_taiga");
    public static final ResourceKey<Biome> BETA_TUNDRA = keyOf("beta_tundra");
    public static final ResourceKey<Biome> BETA_ICE_DESERT = keyOf("beta_ice_desert");

    public static final ResourceKey<Biome> BETA_OCEAN = keyOf("beta_ocean");
    public static final ResourceKey<Biome> BETA_LUKEWARM_OCEAN = keyOf("beta_lukewarm_ocean");
    public static final ResourceKey<Biome> BETA_WARM_OCEAN = keyOf("beta_warm_ocean");
    public static final ResourceKey<Biome> BETA_COLD_OCEAN = keyOf("beta_cold_ocean");
    public static final ResourceKey<Biome> BETA_FROZEN_OCEAN = keyOf("beta_frozen_ocean");

    public static final ResourceKey<Biome> BETA_SKY = keyOf("beta_sky");

    public static final ResourceKey<Biome> PE_FOREST = keyOf("pe_forest");
    public static final ResourceKey<Biome> PE_SHRUBLAND = keyOf("pe_shrubland");
    public static final ResourceKey<Biome> PE_DESERT = keyOf("pe_desert");
    public static final ResourceKey<Biome> PE_SAVANNA = keyOf("pe_savanna");
    public static final ResourceKey<Biome> PE_PLAINS = keyOf("pe_plains");
    public static final ResourceKey<Biome> PE_SEASONAL_FOREST = keyOf("pe_seasonal_forest");
    public static final ResourceKey<Biome> PE_RAINFOREST = keyOf("pe_rainforest");
    public static final ResourceKey<Biome> PE_SWAMPLAND = keyOf("pe_swampland");
    public static final ResourceKey<Biome> PE_TAIGA = keyOf("pe_taiga");
    public static final ResourceKey<Biome> PE_TUNDRA = keyOf("pe_tundra");
    public static final ResourceKey<Biome> PE_ICE_DESERT = keyOf("pe_ice_desert");

    public static final ResourceKey<Biome> PE_OCEAN = keyOf("pe_ocean");
    public static final ResourceKey<Biome> PE_LUKEWARM_OCEAN = keyOf("pe_lukewarm_ocean");
    public static final ResourceKey<Biome> PE_WARM_OCEAN = keyOf("pe_warm_ocean");
    public static final ResourceKey<Biome> PE_COLD_OCEAN = keyOf("pe_cold_ocean");
    public static final ResourceKey<Biome> PE_FROZEN_OCEAN = keyOf("pe_frozen_ocean");

    public static final ResourceKey<Biome> ALPHA = keyOf("alpha");
    public static final ResourceKey<Biome> ALPHA_WINTER = keyOf("alpha_winter");

    public static final ResourceKey<Biome> INFDEV_611 = keyOf("infdev_611");
    public static final ResourceKey<Biome> INFDEV_420 = keyOf("infdev_420");
    public static final ResourceKey<Biome> INFDEV_415 = keyOf("infdev_415");
    public static final ResourceKey<Biome> INFDEV_227 = keyOf("infdev_227");

    public static final ResourceKey<Biome> INDEV_NORMAL = keyOf("indev_normal");
    public static final ResourceKey<Biome> INDEV_HELL = keyOf("indev_hell");
    public static final ResourceKey<Biome> INDEV_PARADISE = keyOf("indev_paradise");
    public static final ResourceKey<Biome> INDEV_WOODS = keyOf("indev_woods");

    private static ResourceKey<Biome> keyOf(String name) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("moderner_beta", name));
    }
}
