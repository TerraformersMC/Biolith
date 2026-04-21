package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalLong;

@Mixin(value = LevelStem.class, priority = 900)
public class MixinLevelStem {
    /*
     * Neoforge modifies the LevelStem record to have this signature, and they do not use the canonical ctor.
     */
    @Inject(
            method = "Lnet/minecraft/world/level/dimension/LevelStem;<init>(Lnet/minecraft/core/Holder;Lnet/minecraft/world/level/chunk/ChunkGenerator;Ljava/util/OptionalLong;)V",
            at = @At("RETURN")
    )
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void biolith$storeDimensionTypeToBiomeSource(Holder<DimensionType> type, ChunkGenerator generator, OptionalLong seedOverride, CallbackInfo ci) {
        generator.getBiomeSource().biolith$setDimensionType(type);
    }
}
