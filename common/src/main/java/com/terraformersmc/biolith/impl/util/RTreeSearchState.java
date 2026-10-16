package com.terraformersmc.biolith.impl.util;

import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;

public class RTreeSearchState<T> {
    private static final int MAX_DEPTH = 64;

    public Climate.RTree.@Nullable Leaf<T> previousUltimateNode;
    public Climate.RTree.@Nullable Leaf<T> previousPenultimateNode;

    @SuppressWarnings("unchecked") // local array of non-reifiable type
    public final Climate.RTree.Node<T>[][] children = new Climate.RTree.Node[MAX_DEPTH][];
    public final int[] next = new int[MAX_DEPTH];
}
