package com.terraformersmc.biolith.api.biome.sub;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

/**
 * Available noise values in the {@link BiomeParameterTarget} enum:
 * <ul>
 * <li>CONTINENTALNESS</li>
 * <li>DEPTH</li>
 * <li>EROSION</li>
 * <li>HUMIDITY</li>
 * <li>PEAKS_VALLEYS</li>
 * <li>TEMPERATURE</li>
 * <li>WEIRDNESS</li>
 * </ul>
 */
public enum BiomeParameterTarget implements StringIdentifiable {
    /*
     * Do not add before or modify the order of the first six names;
     * they must be in the same order as Mojang's parameter arrays.
     * TEMPERATURE, HUMIDITY, CONTINENTALNESS, EROSION, DEPTH, WEIRDNESS, (OFFSET is not in NoiseValuePoint)
     */
    TEMPERATURE("temperature"),
    HUMIDITY("humidity"),
    CONTINENTALNESS("continentalness"),
    EROSION("erosion"),
    DEPTH("depth"),
    WEIRDNESS("weirdness"),
    PEAKS_VALLEYS("peaks_valleys");

    public static final Codec<BiomeParameterTarget> CODEC = StringIdentifiable.createCodec(BiomeParameterTarget::values);
    private final String name;

    BiomeParameterTarget(String name) {
        this.name = name;
    }

    /**
     * @param noisePoint A {@link MultiNoiseUtil.NoiseValuePoint} from which to select a value
     * @return The noise value selected by this parameter target (long-type, not float-type)
     */
    public long getNoiseValue(MultiNoiseUtil.NoiseValuePoint noisePoint) {
        return
            switch (this) {
                case TEMPERATURE -> noisePoint.temperatureNoise();
                case HUMIDITY -> noisePoint.humidityNoise();
                case CONTINENTALNESS -> noisePoint.continentalnessNoise();
                case EROSION -> noisePoint.erosionNoise();
                case DEPTH -> noisePoint.depth();
                case WEIRDNESS -> noisePoint.weirdnessNoise();
                case PEAKS_VALLEYS -> BiomeParameterTarget.getPeaksValleysNoiseLong(noisePoint.weirdnessNoise());
            };
    }

    @Override
    public String asString() {
        return this.name;
    }

    /**
     * <p>
     * This is a faster, more accurate approximation of the following code, and is intended to be used
     * when performing long-type calculations requiring peaks_valleys noise from long-type weirdness noise.
     * </p><p>
     * {@code MultiNoiseUtil.toLong(DensityFunctions.getPeaksValleysNoise(MultiNoiseUtil.toFloat(weirdness))); }
     * </p>
     *
     * @param weirdness A long-type weirdness noise value
     * @return The long-type calculated peaks_valleys noise value
     */
    public static long getPeaksValleysNoiseLong(long weirdness) {
        /*
         * 1.19.3 defines getPeaksValleysNoise() as follows:
         *     public static float getPeaksValleysNoise(float weirdness) {
         *         return -(Math.abs(Math.abs(weirdness) - 0.6666667f) - 0.33333334f) * 3.0f;
         *     }
         */
        return 10000L - Math.abs(Math.abs(weirdness * 3L) - 20000L);
    }

    /**
     * @param range A {@link MultiNoiseUtil.ParameterRange parameter range} for which to find the center
     * @return The long-type center of the provided parameter range
     */
    public static long parameterCenter(MultiNoiseUtil.ParameterRange range) {
        return (range.min() + range.max()) / 2L;
    }

    /**
     * @param parameters A {@link MultiNoiseUtil.ParameterRange parameter range array} for which to find the center
     * @return The {@link MultiNoiseUtil.NoiseValuePoint center point} of the provided parameter range
     */
    public static MultiNoiseUtil.NoiseValuePoint parametersCenterPoint(MultiNoiseUtil.ParameterRange[] parameters) {
        return new MultiNoiseUtil.NoiseValuePoint(
                parameterCenter(parameters[0]),
                parameterCenter(parameters[1]),
                parameterCenter(parameters[2]),
                parameterCenter(parameters[3]),
                parameterCenter(parameters[4]),
                parameterCenter(parameters[5])
        );
    }

    /**
     * @param point1 A {@link MultiNoiseUtil.NoiseValuePoint noise point} for comparison
     * @param point2 A {@link MultiNoiseUtil.NoiseValuePoint noise point} for comparison
     * @param offset The long-type value of an offset to add to the squared distance
     * @return The long-type squared distance between the two noise points
     */
    public static long getSquaredDistance(MultiNoiseUtil.NoiseValuePoint point1, MultiNoiseUtil.NoiseValuePoint point2, long offset) {
        return (MathHelper.square(point1.temperatureNoise() - point2.temperatureNoise()) +
                MathHelper.square(point1.humidityNoise() - point2.humidityNoise()) +
                MathHelper.square(point1.continentalnessNoise() - point2.continentalnessNoise()) +
                MathHelper.square(point1.erosionNoise() - point2.erosionNoise()) +
                MathHelper.square(point1.depth() - point2.depth()) +
                MathHelper.square(point1.weirdnessNoise() - point2.weirdnessNoise()) +
                MathHelper.square(offset)
        );
    }

    /**
     * @param point1 A {@link MultiNoiseUtil.NoiseValuePoint noise point} for comparison
     * @param point2 A {@link MultiNoiseUtil.NoiseValuePoint noise point} for comparison
     * @return The long-type squared distance between the two noise points
     */
    @SuppressWarnings("unused")
    public static long getSquaredDistance(MultiNoiseUtil.NoiseValuePoint point1, MultiNoiseUtil.NoiseValuePoint point2) {
        return getSquaredDistance(point1, point2, 0L);
    }
}
