package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelStem.class, priority = 900)
public class MixinLevelStem {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void biolith$storeDimensionTypeToBiomeSource(Holder<DimensionType> dimensionTypeEntry, ChunkGenerator chunkGenerator, CallbackInfo ci) {
        chunkGenerator.getBiomeSource().biolith$setDimensionType(dimensionTypeEntry);
    }
}
