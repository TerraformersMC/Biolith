package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import net.minecraft.world.level.biome.Climate;

@SuppressWarnings("unused")
public interface InterfaceClimateRTree<T> {
    default BiolithFittestNodes<T> biolith$searchTreeGet(Climate.TargetPoint point, Climate.DistanceMetric<T> distanceFunction) {
        throw new UnsupportedOperationException("InterfaceClimateRTree.biolith$searchTreeGet called on unimplemented interface.");
    }

    // Used to extend injected interface coverage to the forges (f.e. building with Unimined)
    static <T> InterfaceClimateRTree<T> cast(Climate.RTree<T> tree) {
        // Types cannot be inspected at run-time, but we know exactly what the type always is...
        //noinspection unchecked
        return (InterfaceClimateRTree<T>) tree;
    }
}
