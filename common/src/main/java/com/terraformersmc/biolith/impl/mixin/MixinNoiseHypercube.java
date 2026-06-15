package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/*
 * As it turns out, some other mods appear to be serializing the possibleBiomes list at save.
 * This causes errors during serialization when the list contains our out-of-range biomes.
 * At least for 1.20.1, I am going to take the easy cheesy way out and override the limits.
 * See also: MixinParameterRange
 */
@Mixin(MultiNoiseUtil.NoiseHypercube.class)
public class MixinNoiseHypercube {
    @WrapOperation(
            method = "method_28425",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/Codec;floatRange(FF)Lcom/mojang/serialization/Codec;",
                    ordinal = 0
            )
    )
    private static Codec<Float> biolith$relaxBiomeParameterOffset(float minInclusive, float maxInclusive, Operation<Codec<Float>> operation) {
        return operation.call(Math.min(minInclusive, 3f), Math.max(maxInclusive, 4f));
    }
}
