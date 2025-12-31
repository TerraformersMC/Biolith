package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import net.minecraft.world.level.biome.Climate;

@SuppressWarnings("unused")
public interface InterfaceSearchTree<T> {
    default BiolithFittestNodes<T> biolith$searchTreeGet(Climate.TargetPoint point, Climate.DistanceMetric<T> distanceFunction) {
        return null;
    }
}
