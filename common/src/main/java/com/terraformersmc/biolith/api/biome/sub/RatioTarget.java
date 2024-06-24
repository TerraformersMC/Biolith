package com.terraformersmc.biolith.api.biome.sub;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

/**
 * Available computed ratios in the {@link RatioTarget} enum:
 * <ul>
 * <li>CENTER</li>
 * <li>EDGE</li>
 * </ul>
 */
public enum RatioTarget implements StringIdentifiable {
    CENTER("center"),
    EDGE("edge");

    public static final Codec<RatioTarget> CODEC = StringIdentifiable.createCodec(RatioTarget::values);
    private final String name;

    RatioTarget(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }
}
