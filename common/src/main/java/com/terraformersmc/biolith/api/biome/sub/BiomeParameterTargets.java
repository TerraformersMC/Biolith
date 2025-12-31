package com.terraformersmc.biolith.api.biome.sub;

import com.mojang.serialization.Codec;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.biome.Climate;

/**
 * Available noise values in the {@link BiomeParameterTargets} enum:
 * <ul>
 * <li>CONTINENTALNESS</li>
 * <li>DEPTH</li>
 * <li>DEPTH_OCEAN</li>
 * <li>EROSION</li>
 * <li>HUMIDITY</li>
 * <li>PEAKS_VALLEYS</li>
 * <li>TEMPERATURE</li>
 * <li>WEIRDNESS</li>
 * </ul>
 */
public enum BiomeParameterTargets implements StringRepresentable {
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
    PEAKS_VALLEYS("peaks_valleys"),
    DEPTH_OCEAN("depth_ocean");

    public static final Codec<BiomeParameterTargets> CODEC = StringRepresentable.fromEnum(BiomeParameterTargets::values);
    private final String name;

    BiomeParameterTargets(String name) {
        this.name = name;
    }

    /**
     * @param noisePoint A {@link Climate.TargetPoint} from which to select a value
     * @return The noise value selected by this parameter target (long-type, not float-type)
     */
    public long getNoiseValue(Climate.TargetPoint noisePoint) {
        return
            switch (this) {
                case TEMPERATURE -> noisePoint.temperature();
                case HUMIDITY -> noisePoint.humidity();
                case CONTINENTALNESS -> noisePoint.continentalness();
                case EROSION -> noisePoint.erosion();
                case DEPTH -> noisePoint.depth();
                case WEIRDNESS -> noisePoint.weirdness();
                case PEAKS_VALLEYS -> BiomeParameterTargets.getPeaksValleysNoiseLong(noisePoint.weirdness());
                case DEPTH_OCEAN -> BiomeParameterTargets.getDepthWithOceanSurfaceLong(noisePoint.depth());
            };
    }

    @Override
    public String getSerializedName() {
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
     * Compromise method to provide a way to gauge how far below the ocean surface a position is.
     *
     * This method may be removed in a later API version (Biolith 4 or above) if new features make it redundant.
     *
     * @param depth A long-type depth noise value
     * @return The long-type calculated depth with ocean surface
     */
    public static long getDepthWithOceanSurfaceLong(long depth) {
        ServerLevel world = DimensionBiomePlacement.getEvaluatingWorld();
        int seaLevel = world.getSeaLevel();
        double bottom = world.getMinY();
        double top = world.getMaxY();
        double bottomNew = 15000;
        double topNew = -15000;

        return Math.max(depth,
                (long) Mth.clampedMap(QuartPos.toBlock(DimensionBiomePlacement.getEvaluatingBiomePos().getY()), bottom, top, bottomNew, topNew) -
                (long) Mth.clampedMap(seaLevel, bottom, top, bottomNew, topNew));
    }

    /**
     * @param range A {@link Climate.Parameter parameter range} for which to find the center
     * @return The long-type center of the provided parameter range
     */
    public static long parameterCenter(Climate.Parameter range) {
        return (range.min() + range.max()) / 2L;
    }

    /**
     * @param parameters A {@link Climate.Parameter parameter range array} for which to find the center
     * @return The {@link Climate.TargetPoint center point} of the provided parameter range
     */
    public static Climate.TargetPoint parametersCenterPoint(Climate.Parameter[] parameters) {
        return new Climate.TargetPoint(
                parameterCenter(parameters[0]),
                parameterCenter(parameters[1]),
                parameterCenter(parameters[2]),
                parameterCenter(parameters[3]),
                parameterCenter(parameters[4]),
                parameterCenter(parameters[5])
        );
    }

    /**
     * @param point1 A {@link Climate.TargetPoint noise point} for comparison
     * @param point2 A {@link Climate.TargetPoint noise point} for comparison
     * @param offset The long-type value of an offset to add to the squared distance
     * @return The long-type squared distance between the two noise points
     */
    public static long getSquaredDistance(Climate.TargetPoint point1, Climate.TargetPoint point2, long offset) {
        return (Mth.square(point1.temperature() - point2.temperature()) +
                Mth.square(point1.humidity() - point2.humidity()) +
                Mth.square(point1.continentalness() - point2.continentalness()) +
                Mth.square(point1.erosion() - point2.erosion()) +
                Mth.square(point1.depth() - point2.depth()) +
                Mth.square(point1.weirdness() - point2.weirdness()) +
                Mth.square(offset)
        );
    }

    /**
     * @param point1 A {@link Climate.TargetPoint noise point} for comparison
     * @param point2 A {@link Climate.TargetPoint noise point} for comparison
     * @return The long-type squared distance between the two noise points
     */
    @SuppressWarnings("unused")
    public static long getSquaredDistance(Climate.TargetPoint point1, Climate.TargetPoint point2) {
        return getSquaredDistance(point1, point2, 0L);
    }
}
