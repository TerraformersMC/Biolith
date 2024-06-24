package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.InterfaceSearchTree;
import com.terraformersmc.biolith.impl.biome.SimpleArrayIterator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;

@Mixin(MultiNoiseUtil.SearchTree.class)
public class MixinSearchTree<T> implements InterfaceSearchTree<T> {
    @Shadow
    @Final
    private MultiNoiseUtil.SearchTree.TreeNode<T> firstNode;

    private final ThreadLocal<MultiNoiseUtil.SearchTree.TreeLeafNode<T>> previousUltimateNode = new ThreadLocal<>();
    private final ThreadLocal<MultiNoiseUtil.SearchTree.TreeLeafNode<T>> previousPenultimateNode = new ThreadLocal<>();

    /*
     * This is a flattened, stack-based implementation of Mojang's recursive RTree search.
     * We also find the second-best fit, which can significantly expand the scope of the traversal.
     * Because of this, we needed an implementation with better performance than Mojang's.
     */
    @Override
    public BiolithFittestNodes<T> biolith$searchTreeGet(MultiNoiseUtil.NoiseValuePoint point, MultiNoiseUtil.NodeDistanceFunction<T> distanceFunction) {
        long[] otherParameters = point.getNoiseValueList();
        MultiNoiseUtil.SearchTree.TreeNode<T> node = firstNode;
        long nodeDistance = Long.MAX_VALUE;

        @SuppressWarnings("unchecked") // local array of non-reifiable type
        Iterator<MultiNoiseUtil.SearchTree.TreeNode<T>>[] stack = new SimpleArrayIterator[64];
        int stackDepth = 0;

        MultiNoiseUtil.SearchTree.TreeLeafNode<T> ultimate = previousUltimateNode.get();
        MultiNoiseUtil.SearchTree.TreeLeafNode<T> penultimate = previousPenultimateNode.get();

        long ultimateDistance = ultimate != null ? distanceFunction.getDistance(ultimate, otherParameters) : Long.MAX_VALUE;
        long penultimateDistance = penultimate != null ? distanceFunction.getDistance(penultimate, otherParameters) : Long.MAX_VALUE;

        // It's possible for the best and next-best fit to have switched places
        if (ultimateDistance > penultimateDistance) {
            MultiNoiseUtil.SearchTree.TreeLeafNode<T> temp = ultimate;
            ultimate = penultimate;
            penultimate = temp;

            long tempDistance = ultimateDistance;
            ultimateDistance = penultimateDistance;
            penultimateDistance = tempDistance;
        }

        // Prime the stack with the root node, or short-circuit if the tree is malformed
        // Enclosing braces are to force the compiler not to leak the pattern variables out of scope
        {
            if (node instanceof MultiNoiseUtil.SearchTree.TreeBranchNode<T> branchNode) {
                stack[stackDepth] = new SimpleArrayIterator<>(branchNode.subTree);
            } else if (node instanceof MultiNoiseUtil.SearchTree.TreeLeafNode<T> leafNode) {
                // TODO:  Turns out this is actually a thing.  Consider the implications.
                //        Maybe implement some system to warn exactly once.
                //Biolith.LOGGER.warn("Only one biome is available in MultiNoiseBiomeSource!");
                return new BiolithFittestNodes<>(leafNode, distanceFunction.getDistance(leafNode, otherParameters));
            } else {
                // This should not occur; it would imply there are no biomes available
                Biolith.LOGGER.error("No biomes are available in MultiNoiseBiomeSource!");
                return new BiolithFittestNodes<>(ultimate, ultimateDistance);
            }
        }

        // Iteratively search the tree using a stack of simple array iterators to track our position
        while (stack[stackDepth].hasNext()) {
            // Advance to the next available branch node
            node = stack[stackDepth].next();
            nodeDistance = distanceFunction.getDistance(node, otherParameters);

            // Descend the branch until we find a leaf or the branch is no longer fitter
            while (node instanceof MultiNoiseUtil.SearchTree.TreeBranchNode<T> branchNode && penultimateDistance > nodeDistance) {
                stack[++stackDepth] = new SimpleArrayIterator<>(branchNode.subTree);
                node = stack[stackDepth].next();
                nodeDistance = distanceFunction.getDistance(node, otherParameters);
            }

            // If we're at a leaf, it may be fitter
            if (node instanceof MultiNoiseUtil.SearchTree.TreeLeafNode<T> leafNode && penultimateDistance > nodeDistance) {
                if (ultimateDistance > nodeDistance) {
                    if (!biolith$keyOf(leafNode).getValue().equals(biolith$keyOf(ultimate).getValue())) {
                        penultimateDistance = ultimateDistance;
                        penultimate = ultimate;
                    }
                    ultimateDistance = nodeDistance;
                    ultimate = leafNode;
                } else if (!biolith$keyOf(leafNode).getValue().equals(biolith$keyOf(ultimate).getValue())) {
                    penultimateDistance = nodeDistance;
                    penultimate = leafNode;
                }
            }

            // Ascend the branch until we have something to consume (or we're all done).
            while (stackDepth > 0 && !stack[stackDepth].hasNext()) {
                --stackDepth;
            }
        }

        // Store the first- and (if any) second-best fits we found to use as our search fallback
        previousUltimateNode.set(ultimate);
        previousPenultimateNode.set(penultimate);

        // Return the first- and second-best fit nodes, as well as their fitness (squared n-dimensional distance)
        if (penultimate == null) {
            return new BiolithFittestNodes<>(ultimate, ultimateDistance);
        } else {
            return new BiolithFittestNodes<>(ultimate, ultimateDistance, penultimate, penultimateDistance);
        }
    }

    private @NotNull RegistryKey<?> biolith$keyOf(@Nullable MultiNoiseUtil.SearchTree.TreeLeafNode<T> leafNode) {
        if (leafNode == null) {
            return RegistryKey.of(RegistryKeys.BIOME, Identifier.of(Biolith.MOD_ID, "null"));
        } else {
            return ((RegistryEntry<?>) leafNode.value).getKey().orElseThrow();
        }
    }
}
