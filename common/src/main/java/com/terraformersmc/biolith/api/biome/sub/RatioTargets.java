package com.terraformersmc.biolith.api.biome.sub;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

/**
 * Available computed ratios in the {@link RatioTargets} enum:
 * <ul>
 * <li>CENTER</li>
 * <li>EDGE</li>
 * </ul>
 */
public enum RatioTargets implements StringIdentifiable {
    CENTER("center"),
    EDGE("edge");

    public static final Codec<RatioTargets> CODEC = StringIdentifiable.createCodec(RatioTargets::values);
    private final String name;

    RatioTargets(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }
}
