package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DimensionOptions.class, priority = 900)
public class MixinDimensionOptions {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void biolith$storeDimensionTypeToBiomeSource(RegistryEntry<DimensionType> dimensionTypeEntry, ChunkGenerator chunkGenerator, CallbackInfo ci) {
        chunkGenerator.getBiomeSource().biolith$setDimensionType(dimensionTypeEntry);
    }
}
