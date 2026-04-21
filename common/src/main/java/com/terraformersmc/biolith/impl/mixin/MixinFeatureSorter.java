package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.feature.ResilientPlacedFeatureIndexer;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Function;

@Mixin(FeatureSorter.class)
public class MixinFeatureSorter {
    @Unique
    private static final ThreadLocal<Boolean> BIOLITH_RECURSION = ThreadLocal.withInitial(() -> false);

    @WrapMethod(method = "buildFeaturesPerStep")
    private static <T> List<FeatureSorter.StepFeatureData> biolith$wrapFeatureIndexer(List<T> biomes, Function<T, List<HolderSet<PlacedFeature>>> biomesToPlacedFeaturesList, boolean listInvolvedBiomesOnFailure, Operation<List<FeatureSorter.StepFeatureData>> original) {
        List<FeatureSorter.StepFeatureData> features;

        if (Biolith.getConfigManager().getGeneralConfig().forceResilientFeatureIndexer()) {
            Biolith.LOGGER.info("Using Biolith's resilient feature indexer (force_resilient_feature_indexer = true).");
            features = ResilientPlacedFeatureIndexer.collectIndexedFeatures(biomes, biomesToPlacedFeaturesList);
        } else if (BIOLITH_RECURSION.get()) {
            // The original is recursive; don't catch its exceptions when it's calling itself.
            features = original.call(biomes, biomesToPlacedFeaturesList, listInvolvedBiomesOnFailure);
        } else {
            BIOLITH_RECURSION.set(true);
            try {
                features = original.call(biomes, biomesToPlacedFeaturesList, listInvolvedBiomesOnFailure);
            } catch (IllegalStateException e) {
                Biolith.LOGGER.warn(e.getMessage());
                Biolith.LOGGER.warn("Vanilla feature indexer failed; retrying with Biolith's resilient feature indexer...");
                features = ResilientPlacedFeatureIndexer.collectIndexedFeatures(biomes, biomesToPlacedFeaturesList);
            }
            BIOLITH_RECURSION.set(false);
        }

        return features;
    }
}
