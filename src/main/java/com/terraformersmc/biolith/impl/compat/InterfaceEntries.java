package com.terraformersmc.biolith.impl.compat;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Calls to the methods of this interface in instances of MultiNoiseUtil.Entries
 * must always be guarded by an instanceof check.
 *
 * @param <T> In practice T is always {@code RegistryEntry<Biome> }
 */
@SuppressWarnings("unused")
public interface InterfaceEntries<T> {
    /**
     * @return boolean TerraBlender's 'initialized' field in MultiNoiseUtil.Entries
     */
    default boolean biolith$getInitialized() {
        return false;
    }

    /**
     * @return boolean TerraBlender's 'treesPopulated' field in MultiNoiseUtil.Entries
     */
    default boolean biolith$getTreesPopulated() {
        return false;
    }

    /**
     * The Object returned by this method may be safely cast to terrablender.worldgen.noise.Area
     * if TerraBlender is loaded (otherwise the class will be undefined).
     *
     * @return Object TerraBlender's 'uniqueness' field in MultiNoiseUtil.Entries
     */
    default Object biolith$getUniqueness() {
        return null;
    }

    /**
     * @return MultiNoiseUtil.SearchTree<T>[] TerraBlender's 'uniqueTrees' field in MultiNoiseUtil.Entries
     */
    default MultiNoiseUtil.SearchTree<T>[] biolith$getUniqueTrees() {
        return null;
    }

    /**
     * @param x int The x biome coordinate
     * @param y int The y biome coordinate
     * @param z int The z biome coordinate
     * @return @Nullable MultiNoiseUtil.SearchTree<T> TerraBlender's selected RTree for the given location,
     *         or null if TerraBlender is not responsible for biome placement in the relevant dimension.
     */
    @Nullable
    default MultiNoiseUtil.SearchTree<T> biolith$getuniqueTree(int x, int y, int z) {
        return null;
    }
}
