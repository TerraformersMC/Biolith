package com.terraformersmc.biolith.api.biome;

import net.minecraft.world.level.biome.Climate;

@SuppressWarnings("unused")
public record BiolithFittestNodes<T>(Climate.RTree.Leaf<T> ultimate, long ultimateDistance, Climate.RTree.Leaf<T> penultimate, long penultimateDistance) {
    public BiolithFittestNodes(Climate.RTree.Leaf<T> ultimate, long ultimateDistance) {
        this(ultimate, ultimateDistance, null, Long.MAX_VALUE);
    }

    public BiolithFittestNodes<T> of(Climate.RTree.Leaf<T> ultimate, long ultimateDistance, Climate.RTree.Leaf<T> penultimate, long penultimateDistance) {
        return new BiolithFittestNodes<>(ultimate, ultimateDistance, penultimate, penultimateDistance);
    }

    public BiolithFittestNodes<T> withPenultimate(Climate.RTree.Leaf<T> penultimate, long penultimateDistance) {
        return new BiolithFittestNodes<>(ultimate, ultimateDistance, penultimate, penultimateDistance);
    }
}
