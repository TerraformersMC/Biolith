package com.terraformersmc.biolith.api.compat;

import com.terraformersmc.biolith.impl.compat.BiolithCompat;

/**
 * <p>
 * BiolithCompats can be used to check whether Biolith has enabled a compatibility mode.
 * </p>
 * <p>
 * For example:
 * </p>
 * <pre>{@code
 * if (BiolithCompats.isCompatEnabled(BiolithCompats.MODERN_BETA)) {
 *     BiomePlacement.replaceOverworld(ModernBetaBiomeKeys.BETA_FOREST, FOREST_A, 0.4D);
 *     BiomePlacement.replaceOverworld(ModernBetaBiomeKeys.BETA_FOREST, FOREST_B, 0.4D);
 * }
 * }</pre>
 */
@SuppressWarnings("unused")
public enum BiolithCompats {
    MODERN_BETA,
    TERRABLENDER,
    VANILLA;

    public static boolean isCompatEnabled(BiolithCompats compat) {
        return switch (compat) {
            case MODERN_BETA -> BiolithCompat.COMPAT_MODERN_BETA;
            case TERRABLENDER -> BiolithCompat.COMPAT_TERRABLENDER;
            case VANILLA -> true;
        };
    }
}