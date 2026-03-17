package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.compat.VanillaCompat;
import com.terraformersmc.biolith.impl.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

// Inject before TerraBlender so we can ensure our tree search and placement overrides get used.
@Mixin(value = MultiNoiseBiomeSource.class, priority = 900)
public abstract class MixinMultiNoiseBiomeSource extends BiomeSource {
    @Shadow
    protected abstract Climate.ParameterList<Holder<Biome>> parameters();

    @Unique
    private Climate.@Nullable ParameterList<Holder<Biome>> biolith$biomeEntries;

    // Inject noise points the first time somebody requests them.
    @WrapOperation(
            method = "parameters",
            at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;")
    )
    @SuppressWarnings("unused")
    private Object biolith$injectEntries(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> instance, Function<Climate.ParameterList<Holder<Biome>>, Climate.ParameterList<Holder<Biome>>> leftMap, Function<Holder<MultiNoiseBiomeSourceParameterList>, Climate.ParameterList<Holder<Biome>>> rightMap, Operation<Object> original) {
        synchronized (this) {
            // Only compute this once, since our version is more expensive than Mojang's.
            if (biolith$biomeEntries == null) {
                // Mojang does the exact same cast on the return of this operation.
                //noinspection unchecked
                Climate.ParameterList<Holder<Biome>> originalEntries =
                        (Climate.ParameterList<Holder<Biome>>) original.call(instance, leftMap, rightMap);

                if (this.biolith$getDimensionType().identifier().equals(BuiltinDimensionTypes.OVERWORLD.identifier())) {
                    List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameterList = new ArrayList<>(256);

                    // Remove any biomes matching removals
                    originalEntries.values().stream()
                            .filter(BiomeCoordinator.OVERWORLD::removalFilter)
                            .forEach(parameterList::add);

                    // Add all biomes from additions, replacements, and sub-biome requests
                    BiomeCoordinator.OVERWORLD.writeBiomeEntries(parameterList::add);

                    biolith$biomeEntries = new Climate.ParameterList<>(parameterList);
                } else if (this.biolith$getDimensionType().identifier().equals(BuiltinDimensionTypes.NETHER.identifier())) {
                    List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameterList = new ArrayList<>(64);

                    // Remove any biomes matching removals
                    originalEntries.values().stream()
                            .filter(BiomeCoordinator.NETHER::removalFilter)
                            .forEach(parameterList::add);

                    // Add all biomes from additions, replacements, and sub-biome requests
                    BiomeCoordinator.NETHER.writeBiomeEntries(parameterList::add);

                    biolith$biomeEntries = new Climate.ParameterList<>(parameterList);
                } else {
                    biolith$biomeEntries = originalEntries;
                }
            }
        } // synchronized (this)

        return biolith$biomeEntries;
    }

    // We calculate the vanilla/datapack biome, then we apply any overlays.
    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("HEAD"), cancellable = true)
    private void biolith$getBiome(int x, int y, int z, Climate.Sampler noise, CallbackInfoReturnable<Holder<Biome>> cir) {
        Climate.TargetPoint noisePoint = noise.sample(x, y, z);
        BiolithFittestNodes<Holder<Biome>> fittestNodes = null;

        // Find the biome via TerraBlender if available.
        if (BiolithCompat.COMPAT_TERRABLENDER) {
            fittestNodes = Services.PLATFORM.getTerraBlenderCompat().getBiome(x, y, z, noisePoint, parameters());
        }

        // Find the biome via Vanilla (including datapacks) if none was provided by TerraBlender.
        if (fittestNodes == null) {
            fittestNodes = VanillaCompat.getBiome(noisePoint, parameters());
        }

        // Apply biome overlays.
        if (this.biolith$getDimensionType().identifier().equals(BuiltinDimensionTypes.OVERWORLD.identifier())) {
            cir.setReturnValue(BiomeCoordinator.OVERWORLD.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else if (this.biolith$getDimensionType().identifier().equals(BuiltinDimensionTypes.NETHER.identifier())) {
            cir.setReturnValue(BiomeCoordinator.NETHER.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else {
            cir.setReturnValue(fittestNodes.ultimate().value);
        }
    }

    @Override
    public Climate.@Nullable ParameterList<Holder<Biome>> biolith$getBiomeEntries() {
        return biolith$biomeEntries;
    }
}
