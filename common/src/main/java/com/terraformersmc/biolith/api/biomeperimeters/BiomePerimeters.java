package com.terraformersmc.biolith.api.biomeperimeters;

import com.terraformersmc.biolith.impl.biomeperimeters.BiomePerimetersImpl;
import net.minecraft.util.math.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.jetbrains.annotations.NotNull;

/**
 * This class builds a bidirectional hashed list of the points (voxels) on the perimeter of a biome instance.
 * The points are used to provide an estimate of how far "in-biome" a given biome voxel is.  This can allow
 * biome generation (f.e. surface builders) to blend with surrounding biomes or generate context-sensitive
 * terrain heights within a particular biome.
 * <p></p>
 * Note:  In order to achieve acceptable performance in-game, BiomePerimeters makes heavy use of caching and
 * also accepts certain compromises with respect to the accuracy of the perimeter distance values.  It is safe
 * to call getPerimeterDistance() for every individual block column during generation, but on the other hand,
 * minor discontinuities and variations may occasionally occur in the distance values.
 */
@SuppressWarnings("unused")
public interface BiomePerimeters {
	/**
	 * Call this method when you need to know how far in-biome a block column is.  The returned int will give the
	 * distance to the perimeter if it is less than the instance's configured horizon, and a value greater than or
	 * equal to the configured horizon if it is not.
	 *
	 * @param biomeAccess Biome access used to determine whether neighboring voxels are in-biome.
	 * @param pos         The voxel being evaluated for perimeter distance; the Y value is used for biome checks.
	 * @return            The perimeter distance value resolved for the target voxel.
	 */
	int getPerimeterDistance(BiomeAccess biomeAccess, BlockPos pos);

	/**
	 * Each Biome must have a separate instance of BiomePerimeters.  An instance of BiomePerimeters
	 * will be created if one does not already exist, and subsequently the same instance will always
	 * be returned for the given Biome during the same game session.
	 *
	 * @param biome   The Biome for which we are maybe-adding and fetching the BiomePerimeters instance
	 * @param horizon Max distance to check for biome edge; range [1 - 256]
	 */
	static @NotNull BiomePerimeters getOrCreateInstance(@NotNull Biome biome, int horizon) {
		return BiomePerimetersImpl.getOrCreateInstance(biome, horizon);
	}
}
