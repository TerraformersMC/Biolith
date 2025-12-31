package com.terraformersmc.biolith.api.biome.sub;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Available computed ratios in the {@link RatioTargets} enum:
 * <ul>
 * <li>CENTER</li>
 * <li>EDGE</li>
 * </ul>
 */
public enum RatioTargets implements StringRepresentable {
    CENTER("center"),
    EDGE("edge");

    public static final Codec<RatioTargets> CODEC = StringRepresentable.fromEnum(RatioTargets::values);
    private final String name;

    RatioTargets(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
