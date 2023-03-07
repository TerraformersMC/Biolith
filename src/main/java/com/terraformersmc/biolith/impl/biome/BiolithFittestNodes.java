package com.terraformersmc.biolith.impl.biome;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public record BiolithFittestNodes<T>(MultiNoiseUtil.SearchTree.TreeLeafNode<T> ultimate, long ultimateDistance, MultiNoiseUtil.SearchTree.TreeLeafNode<T> penultimate, long penultimateDistance) {
    public BiolithFittestNodes(MultiNoiseUtil.SearchTree.TreeLeafNode<T> ultimate, long ultimateDistance) {
        this(ultimate, ultimateDistance, null, Long.MAX_VALUE);
    }

    public BiolithFittestNodes<T> of(MultiNoiseUtil.SearchTree.TreeLeafNode<T> ultimate, long ultimateDistance, MultiNoiseUtil.SearchTree.TreeLeafNode<T> penultimate, long penultimateDistance) {
        return new BiolithFittestNodes<T>(ultimate, ultimateDistance, penultimate, penultimateDistance);
    }

    public BiolithFittestNodes<T> withPenultimate(MultiNoiseUtil.SearchTree.TreeLeafNode<T> penultimate, long penultimateDistance) {
        return new BiolithFittestNodes<T>(ultimate, ultimateDistance, penultimate, penultimateDistance);
    }
}
