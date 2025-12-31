package com.terraformersmc.biolith.impl.feature;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.*;
import com.terraformersmc.biolith.impl.Biolith;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

@SuppressWarnings("UnstableApiUsage")
public class ResilientPlacedFeatureIndexer {
    private static final IndexedFeature ROOT_FEATURE = new IndexedFeature(Holder.direct(new PlacedFeature(null, null)), -1);

    public static <T> List<FeatureSorter.StepFeatureData> collectIndexedFeatures(List<T> biomes, Function<T, List<HolderSet<PlacedFeature>>> biomesToPlacedFeaturesList) {
        int featureSteps = 0;

        MutableGraph<IndexedFeature> graph = GraphBuilder
                .directed()
                .expectedNodeCount(1024)
                .nodeOrder(ElementOrder.insertion())
                .incidentEdgeOrder(ElementOrder.stable())
                .allowsSelfLoops(false)
                .build();

        // Generate DAG including all non-cycle-forming edges representing ordering relationships.
        graph.addNode(ROOT_FEATURE);
        for (T biome : biomes) {
            List<HolderSet<PlacedFeature>> featureEntryLists = biomesToPlacedFeaturesList.apply(biome);
            featureSteps = Math.max(featureSteps, featureEntryLists.size());
            IndexedFeature previous = ROOT_FEATURE;

            for (int step = 0; step < featureEntryLists.size(); ++step) {
                for (Holder<PlacedFeature> featureEntry : featureEntryLists.get(step)) {
                    IndexedFeature current = new IndexedFeature(featureEntry, step);

                    if (graph.nodes().contains(current) && Graphs.reachableNodes(graph, current).contains(previous)) {
                        Biolith.LOGGER.info("Dropping cycle-forming edge in biome {}: {} -> {}", getEntryString(biome), previous, current);
                    } else {
                        try {
                            graph.putEdge(previous, current);
                        } catch (IllegalArgumentException e) {
                            Biolith.LOGGER.info("Dropping self-edge in biome {}: {}", getEntryString(biome), current);
                        }
                    }

                    previous = current;
                }
            }
        }

        // Storage for staging from DAG to IndexedFeatures; Java is not smart about array types.
        //noinspection unchecked
        List<PlacedFeature>[] stepwiseLists = new ArrayList[featureSteps];
        for (int step = 0; step < featureSteps; ++step) {
            stepwiseLists[step] = new ArrayList<>(128);
        }

        // Traverse DAG outputting to ordered lists of stepwise feature placements.
        Traverser.forGraph(graph).depthFirstPostOrder(ROOT_FEATURE)
                .forEach(node -> {
                    if (node.step >= 0) {
                        stepwiseLists[node.step].add(node.featureEntry.value());
                    }
                });

        // Depth first post-order generates a reversed list.
        // Ignore the compiler warning about access; it's wrong about that and I can't find the suppression.
        ImmutableList.Builder<FeatureSorter.StepFeatureData> builder = ImmutableList.builder();
        for (int step = 0; step < featureSteps; ++step) {
            Collections.reverse(stepwiseLists[step]);
            builder.add(new FeatureSorter.StepFeatureData(stepwiseLists[step]));
        }

        return builder.build();
    }

    // Data record for indexing features; provides convenience methods.
    record IndexedFeature(Holder<PlacedFeature> featureEntry, int step) {
        @Override
        public boolean equals(Object o) {
            //noinspection DeconstructionCanBeUsed
            return o instanceof IndexedFeature other &&
                    this.step == other.step &&
                    this.featureEntry.equals(other.featureEntry);
        }

        @Override
        public int hashCode() {
            return this.featureEntry.hashCode() ^ this.step;
        }

        @Override
        public @NotNull String toString() {
            return featureEntry.getRegisteredName();
        }
    }

    // Deal with annoying templating in these classes.
    private static <T> String getEntryString(T thing) {
        if (thing instanceof Holder<?> entry) {
            return entry.getRegisteredName();
        } else {
            return thing.toString();
        }
    }
}
