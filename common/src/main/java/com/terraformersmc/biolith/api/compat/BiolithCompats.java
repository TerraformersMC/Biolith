package com.terraformersmc.biolith.api.compat;

import com.terraformersmc.biolith.impl.compat.BiolithCompat;

/**
 * <p>
 * BiolithCompats can be used to check whether Biolith has enabled a compatibility mode.
 * </p><p>
 * For example:
 * </p>
 * <pre>{@code
 * if (BiolithCompats.isCompatEnabled(BiolithCompats.MODERNER_BETA)) {
 *     BiomePlacement.replaceOverworld(ModernerBetaBiomeKeys.BETA_FOREST, FOREST_A, 0.4D);
 *     BiomePlacement.replaceOverworld(ModernerBetaBiomeKeys.BETA_FOREST, FOREST_B, 0.4D);
 * }
 * }</pre>
 */
@SuppressWarnings("unused")
public enum BiolithCompats {
    /**
     * <p>
     * Compatibility layer for Modern Beta, allowing replacement of biomes in Modern Beta worlds.
     * </p><p>
     * This layer is a temporary alias for {@code MODERNER_BETA}, and will be removed in Biolith 4.
     * </p>
     */
    @Deprecated(forRemoval = true)
    MODERN_BETA,
    /**
     * Compatibility layer for Moderner Beta, allowing replacement of biomes in Moderner Beta worlds.
     */
    MODERNER_BETA,
    /**
     * Compatibility layer for TerraBlender, allowing biome placement and surface rules in TerraBlender regions.
     */
    TERRABLENDER,
    /**
     * Compatibility layer for vanilla Minecraft.  At the moment, this compatibility is always enabled, but
     * in future versions it may be disabled under certain circumstances (f.e. Biolith having full control of
     * worldgen in primary dimensions).
     */
    VANILLA;

    public static boolean isCompatEnabled(BiolithCompats compat) {
        return switch (compat) {
            case MODERN_BETA, MODERNER_BETA -> BiolithCompat.COMPAT_MODERNER_BETA;
            case TERRABLENDER -> BiolithCompat.COMPAT_TERRABLENDER;
            case VANILLA -> true;
        };
    }
}