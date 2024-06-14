package com.terraformersmc.biolith.api.biome.subbiome;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public enum BiomeParameterTarget implements StringIdentifiable {
    CONTINENTALNESS("continentalness"),
    DEPTH("depth"),
    EROSION("erosion"),
    HUMIDITY("humidity"),
    PEAKS_VALLEYS("peaks_valleys"),
    TEMPERATURE("temperature"),
    WEIRDNESS("weirdness");

    public static final Codec<BiomeParameterTarget> CODEC = StringIdentifiable.createCodec(BiomeParameterTarget::values);
    private final String name;

    BiomeParameterTarget(String name) {
        this.name = name;
    }

    public float getNoiseValue(MultiNoiseUtil.NoiseValuePoint noisePoint) {
        return MultiNoiseUtil.toFloat(
            switch (this) {
                case CONTINENTALNESS -> noisePoint.continentalnessNoise();
                case DEPTH -> noisePoint.depth();
                case EROSION -> noisePoint.erosionNoise();
                case HUMIDITY -> noisePoint.humidityNoise();
                case PEAKS_VALLEYS -> getPV(noisePoint.weirdnessNoise());
                case TEMPERATURE -> noisePoint.temperatureNoise();
                case WEIRDNESS -> noisePoint.weirdnessNoise();
            }
        );
    }

    public static long getPV(long weirdness) {
        return 10000L - Math.abs(Math.abs(weirdness * 3L) - 20000L);
    }

    @Override
    public String asString() {
        return this.name;
    }
}
