package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.InterfaceClimateRTree;
import com.terraformersmc.biolith.impl.util.RTreeSearchState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Climate.RTree.class)
public class MixinClimateRTree<T> implements InterfaceClimateRTree<T> {
    @Unique
    private static final ResourceKey<?> BIOLITH_NULL_KEY = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("biolith", "null"));

    @Unique
    private final ThreadLocal<RTreeSearchState<T>> biolith$rTreeSearchState = ThreadLocal.withInitial(RTreeSearchState::new);

    @Shadow
    @Final
    private Climate.RTree.Node<T> root;

    /*
     * This is a flattened, stack-based implementation of Mojang's recursive RTree search.
     * We also find the second-best fit, which can significantly expand the scope of the traversal.
     * Because of this, we needed an implementation with better performance than Mojang's.
     */
    @Override
    public BiolithFittestNodes<T> biolith$searchTreeGet(Climate.TargetPoint point, Climate.DistanceMetric<T> distanceFunction) {
        RTreeSearchState<T> searchState = biolith$rTreeSearchState.get();
        long[] candidate = point.toParameterArray();
        Climate.RTree.Node<T> node = root;
        long nodeDistance = Long.MAX_VALUE;

        Climate.RTree.Node<T>[][] children = searchState.children;
        int[] next = searchState.next;
        int depth = 0;

        Climate.RTree.@Nullable Leaf<T> ultimate = searchState.previousUltimateNode;
        Climate.RTree.@Nullable Leaf<T> penultimate = searchState.previousPenultimateNode;

        long ultimateDistance = ultimate != null ? distanceFunction.distance(ultimate, candidate) : Long.MAX_VALUE;
        long penultimateDistance = penultimate != null ? distanceFunction.distance(penultimate, candidate) : Long.MAX_VALUE;

        // It's possible for the best and next-best fit to have switched places
        if (ultimateDistance > penultimateDistance) {
            Climate.RTree.Leaf<T> temp = ultimate;
            ultimate = penultimate;
            penultimate = temp;

            long tempDistance = ultimateDistance;
            ultimateDistance = penultimateDistance;
            penultimateDistance = tempDistance;
        }

        // Prime the stack with the root node, or short-circuit if the tree is malformed
        // Enclosing braces are to force the compiler not to leak the pattern variables out of scope
        {
            if (node instanceof Climate.RTree.SubTree<T> branchNode) {
                children[0] = branchNode.children;
                next[0] = 0;
            } else if (node instanceof Climate.RTree.Leaf<T> leafNode) {
                // TODO:  Turns out this is actually a thing.  Consider the implications.
                //        Maybe implement some system to warn exactly once.
                //Biolith.LOGGER.warn("Only one biome is available in MultiNoiseBiomeSource!");

                return new BiolithFittestNodes<>(leafNode, distanceFunction.distance(leafNode, candidate));
            } else {
                // This should not occur; it would imply there are no biomes available
                Biolith.LOGGER.error("No biomes are available in MultiNoiseBiomeSource!");

                // Mojang's implementation will return null under the same conditions
                //noinspection ConstantConditions
                return new BiolithFittestNodes<>(ultimate, ultimateDistance);
            }
        }

        // Iteratively depth-first search the tree by tracking our position moving through 2D state arrays
        // This no longer uses an actual stack, for performance reasons, but the behavior is identical
        while (next[depth] < children[depth].length) {
            // Advance to the next available branch node
            node = children[depth][next[depth]++];
            nodeDistance = distanceFunction.distance(node, candidate);

            // Descend the branch until we find a leaf or the branch is no longer fitter
            while (node instanceof Climate.RTree.SubTree<T> branchNode && penultimateDistance > nodeDistance) {
                children[++depth] = branchNode.children;
                node = children[depth][0];
                next[depth] = 1;
                nodeDistance = distanceFunction.distance(node, candidate);
            }

            // If we're at a leaf, it may be fitter
            if (node instanceof Climate.RTree.Leaf<T> leafNode && penultimateDistance > nodeDistance) {
                if (ultimateDistance > nodeDistance) {
                    if (!biolith$keyOf(leafNode).identifier().equals(biolith$keyOf(ultimate).identifier())) {
                        penultimateDistance = ultimateDistance;
                        penultimate = ultimate;
                    }
                    ultimateDistance = nodeDistance;
                    ultimate = leafNode;
                } else if (!biolith$keyOf(leafNode).identifier().equals(biolith$keyOf(ultimate).identifier())) {
                    penultimateDistance = nodeDistance;
                    penultimate = leafNode;
                }
            }

            // Ascend the branch until we have something to consume (or we're all done).
            while (depth > 0 && next[depth] >= children[depth].length) {
                --depth;
            }
        }

        // It is no longer possible for ultimate to be null (the tree is known to have values at this point)
        // (However, the compiler is not sophisticated enough to discern this fact, so we assert it)
        assert ultimate != null;

        // Store the first- and (if any) second-best fits we found to use as our search fallback
        searchState.previousUltimateNode = ultimate;
        searchState.previousPenultimateNode = penultimate;

        // Return the first- and second-best fit nodes, as well as their fitness (squared n-dimensional distance)
        if (penultimate == null) {
            return new BiolithFittestNodes<>(ultimate, ultimateDistance);
        } else {
            return new BiolithFittestNodes<>(ultimate, ultimateDistance, penultimate, penultimateDistance);
        }
    }

    @Unique
    private ResourceKey<?> biolith$keyOf(Climate.RTree.@Nullable Leaf<T> leafNode) {
        if (leafNode == null) {
            return BIOLITH_NULL_KEY;
        } else if (leafNode.value instanceof Holder.Reference<?> reference) {
            return reference.key();
        } else {
            return ((Holder<?>) leafNode.value).unwrapKey().orElseThrow();
        }
    }
}
