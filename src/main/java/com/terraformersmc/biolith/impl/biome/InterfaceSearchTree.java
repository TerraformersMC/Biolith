package com.terraformersmc.biolith.impl.biome;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public interface InterfaceSearchTree<T> {
    BiolithFittestNodes<T> biolith$searchTreeGet(MultiNoiseUtil.NoiseValuePoint point, MultiNoiseUtil.NodeDistanceFunction<T> distanceFunction);
}