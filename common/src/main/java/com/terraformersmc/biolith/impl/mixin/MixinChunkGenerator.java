package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.feature.ResilientPlacedFeatureIndexer;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

@Mixin(value = ChunkGenerator.class, priority = 1100)
public class MixinChunkGenerator {
    /*
     * NeoForge modifies the chunk generator feature list and their implementation
     * breaks Biolith in the End by finalizing a partial list of features.  This
     * mixin bypasses the method which finalizes the list too early.
     */
    @Inject(method = "initializeIndexedFeaturesList", at = @At("HEAD"), cancellable = true)
    private void biolith$disableInitializeIndexedFeaturesList(CallbackInfo ci) {
        ci.cancel();
    }

    /*
     * This is the lambda inside the main ctor at the following location:
     * this.indexedFeaturesListSupplier = Suppliers.memoize( -> HERE <- );
     * We are wrapping vanilla's collectIndexedFeatures() call.
     */
    @WrapOperation(method = "method_44215",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/gen/feature/util/PlacedFeatureIndexer;collectIndexedFeatures(Ljava/util/List;Ljava/util/function/Function;Z)Ljava/util/List;"
            )
    )
    private static <T> List<PlacedFeatureIndexer.IndexedFeatures> biolith$wrapFeatureIndexer(List<T> biomes, Function<T, List<RegistryEntryList<PlacedFeature>>> biomesToPlacedFeaturesList, boolean listInvolvedBiomesOnFailure, Operation<List<PlacedFeatureIndexer.IndexedFeatures>> original) {
        List<PlacedFeatureIndexer.IndexedFeatures> features;

        if (Biolith.getConfigManager().getGeneralConfig().forceResilientFeatureIndexer()) {
            Biolith.LOGGER.info("Using Biolith's resilient feature indexer (force_resilient_feature_indexer = true).");
            features = ResilientPlacedFeatureIndexer.collectIndexedFeatures(biomes, biomesToPlacedFeaturesList);
        } else {
            try {
                features = original.call(biomes, biomesToPlacedFeaturesList, listInvolvedBiomesOnFailure);
            } catch (IllegalStateException e) {
                Biolith.LOGGER.warn(e.getMessage());
                Biolith.LOGGER.warn("Vanilla feature indexer failed; retrying with Biolith's resilient feature indexer...");
                features = ResilientPlacedFeatureIndexer.collectIndexedFeatures(biomes, biomesToPlacedFeaturesList);
            }
        }

        return features;
    }
}
