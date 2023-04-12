package com.terraformersmc.biolith.impl.biome;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;

@SuppressWarnings("unused")
public interface InterfaceSearchTree<T> {
    default BiolithFittestNodes<T> biolith$searchTreeGet(MultiNoiseUtil.NoiseValuePoint point, MultiNoiseUtil.NodeDistanceFunction<T> distanceFunction) {
        return null;
    }
}