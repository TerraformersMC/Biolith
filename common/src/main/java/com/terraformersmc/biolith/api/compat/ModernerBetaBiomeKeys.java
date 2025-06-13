package com.terraformersmc.biolith.api.compat;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

/**
 * <p>
 * Copies of the registry keys of Moderner Beta's biomes.  These keys can be used to
 * target Moderner Beta biomes with Biolith's replacement noise, without depending on
 * Moderner Beta when building.  Moderner Beta biomes can be replaced, but they cannot
 * be placed or targeted with sub-biome matchers.
 * </p><p>
 * For example, to replace 20% of "Beta Forest" with "Forest A" and 20% with "Forest B":
 * </p>
 * <pre>{@code
 * if (BiolithCompats.isCompatEnabled(BiolithCompats.MODERNER_BETA)) {
 *     BiomePlacement.replaceOverworld(ModernerBetaBiomeKeys.BETA_FOREST, FOREST_A, 0.4D);
 *     BiomePlacement.replaceOverworld(ModernerBetaBiomeKeys.BETA_FOREST, FOREST_B, 0.4D);
 * }
 * }</pre>
 */
@SuppressWarnings("unused")
public class ModernerBetaBiomeKeys {
    public static final RegistryKey<Biome> BETA_FOREST = keyOf("beta_forest");
    public static final RegistryKey<Biome> BETA_OAK_FOREST = keyOf("beta_oak_forest");
    public static final RegistryKey<Biome> BETA_SHRUBLAND = keyOf("beta_shrubland");
    public static final RegistryKey<Biome> BETA_DESERT = keyOf("beta_desert");
    public static final RegistryKey<Biome> BETA_SAVANNA = keyOf("beta_savanna");
    public static final RegistryKey<Biome> BETA_PLAINS = keyOf("beta_plains");
    public static final RegistryKey<Biome> BETA_SEASONAL_FOREST = keyOf("beta_seasonal_forest");
    public static final RegistryKey<Biome> BETA_RAINFOREST = keyOf("beta_rainforest");
    public static final RegistryKey<Biome> BETA_SWAMPLAND = keyOf("beta_swampland");
    public static final RegistryKey<Biome> BETA_TAIGA = keyOf("beta_taiga");
    public static final RegistryKey<Biome> BETA_OAK_TAIGA = keyOf("beta_oak_taiga");
    public static final RegistryKey<Biome> BETA_TUNDRA = keyOf("beta_tundra");
    public static final RegistryKey<Biome> BETA_ICE_DESERT = keyOf("beta_ice_desert");

    public static final RegistryKey<Biome> BETA_OCEAN = keyOf("beta_ocean");
    public static final RegistryKey<Biome> BETA_LUKEWARM_OCEAN = keyOf("beta_lukewarm_ocean");
    public static final RegistryKey<Biome> BETA_WARM_OCEAN = keyOf("beta_warm_ocean");
    public static final RegistryKey<Biome> BETA_COLD_OCEAN = keyOf("beta_cold_ocean");
    public static final RegistryKey<Biome> BETA_FROZEN_OCEAN = keyOf("beta_frozen_ocean");

    public static final RegistryKey<Biome> BETA_SKY = keyOf("beta_sky");

    public static final RegistryKey<Biome> PE_FOREST = keyOf("pe_forest");
    public static final RegistryKey<Biome> PE_SHRUBLAND = keyOf("pe_shrubland");
    public static final RegistryKey<Biome> PE_DESERT = keyOf("pe_desert");
    public static final RegistryKey<Biome> PE_SAVANNA = keyOf("pe_savanna");
    public static final RegistryKey<Biome> PE_PLAINS = keyOf("pe_plains");
    public static final RegistryKey<Biome> PE_SEASONAL_FOREST = keyOf("pe_seasonal_forest");
    public static final RegistryKey<Biome> PE_RAINFOREST = keyOf("pe_rainforest");
    public static final RegistryKey<Biome> PE_SWAMPLAND = keyOf("pe_swampland");
    public static final RegistryKey<Biome> PE_TAIGA = keyOf("pe_taiga");
    public static final RegistryKey<Biome> PE_TUNDRA = keyOf("pe_tundra");
    public static final RegistryKey<Biome> PE_ICE_DESERT = keyOf("pe_ice_desert");

    public static final RegistryKey<Biome> PE_OCEAN = keyOf("pe_ocean");
    public static final RegistryKey<Biome> PE_LUKEWARM_OCEAN = keyOf("pe_lukewarm_ocean");
    public static final RegistryKey<Biome> PE_WARM_OCEAN = keyOf("pe_warm_ocean");
    public static final RegistryKey<Biome> PE_COLD_OCEAN = keyOf("pe_cold_ocean");
    public static final RegistryKey<Biome> PE_FROZEN_OCEAN = keyOf("pe_frozen_ocean");

    public static final RegistryKey<Biome> ALPHA = keyOf("alpha");
    public static final RegistryKey<Biome> ALPHA_WINTER = keyOf("alpha_winter");

    public static final RegistryKey<Biome> INFDEV_611 = keyOf("infdev_611");
    public static final RegistryKey<Biome> INFDEV_420 = keyOf("infdev_420");
    public static final RegistryKey<Biome> INFDEV_415 = keyOf("infdev_415");
    public static final RegistryKey<Biome> INFDEV_325 = keyOf("infdev_325");
    public static final RegistryKey<Biome> INFDEV_227 = keyOf("infdev_227");

    public static final RegistryKey<Biome> INDEV_NORMAL = keyOf("indev_normal");
    public static final RegistryKey<Biome> INDEV_HELL = keyOf("indev_hell");
    public static final RegistryKey<Biome> INDEV_PARADISE = keyOf("indev_paradise");
    public static final RegistryKey<Biome> INDEV_WOODS = keyOf("indev_woods");
    public static final RegistryKey<Biome> CLASSIC_14A_08 = keyOf("classic_14a_08");

    public static final RegistryKey<Biome> LATE_BETA_EXTREME_HILLS = keyOf("late_beta_extreme_hills");
    public static final RegistryKey<Biome> LATE_BETA_ICE_PLAINS = keyOf("late_beta_ice_plains");
    public static final RegistryKey<Biome> LATE_BETA_SWAMPLAND = keyOf("late_beta_swampland");
    public static final RegistryKey<Biome> LATE_BETA_PLAINS = keyOf("late_beta_plains");
    public static final RegistryKey<Biome> LATE_BETA_TAIGA = keyOf("late_beta_taiga");

    public static final RegistryKey<Biome> EARLY_RELEASE_ICE_PLAINS = keyOf("early_release_ice_plains");
    public static final RegistryKey<Biome> EARLY_RELEASE_SWAMPLAND = keyOf("early_release_swampland");
    public static final RegistryKey<Biome> EARLY_RELEASE_EXTREME_HILLS = keyOf("early_release_extreme_hills");
    public static final RegistryKey<Biome> EARLY_RELEASE_TAIGA = keyOf("early_release_taiga");

    private static RegistryKey<Biome> keyOf(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, Identifier.of("moderner_beta", name));
    }
}
