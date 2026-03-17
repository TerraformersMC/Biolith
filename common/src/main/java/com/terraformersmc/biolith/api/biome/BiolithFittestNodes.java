package com.terraformersmc.biolith.api.biome;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;

/**
 * Data record representing the results of a biome selection using Biolith's extended biome search.
 *
 * @param ultimate Holder of the first-discovered best-fit biome
 * @param ultimateDistance "Squared" distance from target point to ultimate biome
 * @param penultimate Holder of the next-best-fit (or second-discovered best-fit) biome
 * @param penultimateDistance "Squared" distance from target point to penultimate biome
 * @param <T> is always {@linkplain Holder}<{@linkplain Biome}>
 */
@SuppressWarnings("unused")
public record BiolithFittestNodes<T>(Climate.RTree.Leaf<T> ultimate, long ultimateDistance, Climate.RTree.@Nullable Leaf<T> penultimate, long penultimateDistance) {
    /**
     * Convenience constructor for partial records (i.e. penultimate values are not yet known or do not exist).
     * This is used internally during the setup phase of the biome search.
     *
     * @param ultimate Holder of the first-discovered best-fit biome
     * @param ultimateDistance "Squared" distance from target point to ultimate biome
     */
    public BiolithFittestNodes(Climate.RTree.Leaf<T> ultimate, long ultimateDistance) {
        this(ultimate, ultimateDistance, null, Long.MAX_VALUE);
    }

    /**
     * Creates a new record replacing the penultimate values with the provided new values.
     * This is used internally during the second-best-fit phase of the biome search.
     *
     * @param penultimate Holder of the next-best-fit (or second-discovered best-fit) biome
     * @param penultimateDistance "Squared" distance from target point to penultimate biome
     * @return New, updated {@linkplain BiolithFittestNodes} record
     */
    public BiolithFittestNodes<T> withPenultimate(Climate.RTree.Leaf<T> penultimate, long penultimateDistance) {
        return new BiolithFittestNodes<>(ultimate, ultimateDistance, penultimate, penultimateDistance);
    }
}
