package com.terraformersmc.biolith.impl.feature;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.*;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class ResilientPlacedFeatureIndexer {
    private static final IndexedFeature ROOT_FEATURE = new IndexedFeature(RegistryEntry.of(new PlacedFeature(null, null)), -1);

    public static <T> List<PlacedFeatureIndexer.IndexedFeatures> collectIndexedFeatures(List<T> biomes, Function<T, List<RegistryEntryList<PlacedFeature>>> biomesToPlacedFeaturesList) {
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
            List<RegistryEntryList<PlacedFeature>> featureEntryLists = biomesToPlacedFeaturesList.apply(biome);
            featureSteps = Math.max(featureSteps, featureEntryLists.size());
            IndexedFeature previous = ROOT_FEATURE;

            for (int step = 0; step < featureEntryLists.size(); ++step) {
                for (RegistryEntry<PlacedFeature> featureEntry : featureEntryLists.get(step)) {
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
        ImmutableList.Builder<PlacedFeatureIndexer.IndexedFeatures> builder = ImmutableList.builder();
        for (int step = 0; step < featureSteps; ++step) {
            Collections.reverse(stepwiseLists[step]);
            builder.add(new PlacedFeatureIndexer.IndexedFeatures(stepwiseLists[step]));
        }

        return builder.build();
    }

    // Data record for indexing features; provides convenience methods.
    record IndexedFeature(RegistryEntry<PlacedFeature> featureEntry, int step) {
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
            return featureEntry.getIdAsString();
        }
    }

    // Deal with annoying templating in these classes.
    private static <T> String getEntryString(T thing) {
        if (thing instanceof RegistryEntry<?> entry) {
            return entry.getIdAsString();
        } else {
            return thing.toString();
        }
    }
}
