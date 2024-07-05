package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkGenerator.class, priority = 1100)
public class MixinChunkGenerator {
    @Inject(method = "initializeIndexedFeaturesList", at = @At("HEAD"), cancellable = true)
    private void biolith$disableInitializeIndexedFeaturesList(CallbackInfo ci) {
        /*
         * NeoForge modifies the chunk generator feature list and their implementation
         * breaks Biolith in the End by finalizing a partial list of features.  This
         * mixin bypasses the method which finalizes the list too early.
         */
        ci.cancel();
    }
}
