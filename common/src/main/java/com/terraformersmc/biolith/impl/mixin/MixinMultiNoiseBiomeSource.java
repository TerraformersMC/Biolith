package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.compat.VanillaCompat;
import com.terraformersmc.biolith.impl.platform.Services;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
    protected abstract MultiNoiseUtil.Entries<RegistryEntry<Biome>> getBiomeEntries();

    private MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$biomeEntries;

    // Inject noise points the first time somebody requests them.
    @WrapOperation(
            method = "getBiomeEntries",
            at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;")
    )
    @SuppressWarnings("unused")
    private Object biolith$injectEntries(Either<MultiNoiseUtil.Entries<RegistryEntry<Biome>>, RegistryEntry<MultiNoiseBiomeSourceParameterList>> instance, Function<MultiNoiseUtil.Entries<RegistryEntry<Biome>>, MultiNoiseUtil.Entries<RegistryEntry<Biome>>> leftMap, Function<RegistryEntry<MultiNoiseBiomeSourceParameterList>, MultiNoiseUtil.Entries<RegistryEntry<Biome>>> rightMap, Operation<Object> original) {
        synchronized (this) {
            // Only compute this once, since our version is more expensive than Mojang's.
            if (biolith$biomeEntries == null) {
                // Mojang does the exact same cast on the return of this operation.
                //noinspection unchecked
                MultiNoiseUtil.Entries<RegistryEntry<Biome>> originalEntries =
                        (MultiNoiseUtil.Entries<RegistryEntry<Biome>>) original.call(instance, leftMap, rightMap);

                if (this.biolith$getDimensionType().matchesKey(DimensionTypes.OVERWORLD)) {
                    List<Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>>> parameterList = new ArrayList<>(256);

                    parameterList.addAll(originalEntries.getEntries());
                    BiomeCoordinator.OVERWORLD.writeBiomeEntries(parameterList::add);

                    biolith$biomeEntries = new MultiNoiseUtil.Entries<>(parameterList);
                } else if (this.biolith$getDimensionType().matchesKey(DimensionTypes.THE_NETHER)) {
                    List<Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>>> parameterList = new ArrayList<>(64);

                    parameterList.addAll(originalEntries.getEntries());
                    BiomeCoordinator.NETHER.writeBiomeEntries(parameterList::add);

                    biolith$biomeEntries = new MultiNoiseUtil.Entries<>(parameterList);
                } else {
                    biolith$biomeEntries = originalEntries;
                }
            }
        } // synchronized (this)

        return biolith$biomeEntries;
    }

    // We calculate the vanilla/datapack biome, then we apply any overlays.
    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private void biolith$getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        MultiNoiseUtil.NoiseValuePoint noisePoint = noise.sample(x, y, z);
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes = null;

        // Find the biome via TerraBlender if available.
        if (BiolithCompat.COMPAT_TERRABLENDER) {
            fittestNodes = Services.PLATFORM.getTerraBlenderCompat().getBiome(x, y, z, noisePoint, getBiomeEntries());
        }

        // Find the biome via Vanilla (including datapacks) if none was provided by TerraBlender.
        if (fittestNodes == null) {
            fittestNodes = VanillaCompat.getBiome(noisePoint, getBiomeEntries());
        }

        // Apply biome overlays.
        if (this.biolith$getDimensionType().matchesKey(DimensionTypes.OVERWORLD)) {
            cir.setReturnValue(BiomeCoordinator.OVERWORLD.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else if (this.biolith$getDimensionType().matchesKey(DimensionTypes.THE_NETHER)) {
            cir.setReturnValue(BiomeCoordinator.NETHER.getReplacement(x, y, z, noisePoint, fittestNodes));
        } else {
            cir.setReturnValue(fittestNodes.ultimate().value);
        }
    }

    @Override
    public MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$getBiomeEntries() {
        return biolith$biomeEntries;
    }
}
