package com.terraformersmc.biolith.impl.mixin;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.biome.*;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.compat.VanillaCompat;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mixin(value = TheEndBiomeSource.class, priority = 900)
public abstract class MixinTheEndBiomeSource extends BiomeSource {
    private static RegistryEntryLookup<Biome> biolith$biomeLookup;
    private static MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$biomeEntries;

    @Inject(method = "createVanilla", at = @At("HEAD"))
    private static void biolith$getRegistry(RegistryEntryLookup<Biome> biomeLookup, CallbackInfoReturnable<TheEndBiomeSource> cir) {
        biolith$biomeLookup = biomeLookup;
    }

    @ModifyReturnValue(method = "biomeStream", at = @At("RETURN"))
    @SuppressWarnings("unused")
    private Stream<RegistryEntry<Biome>> biolith$biomeStream(Stream<RegistryEntry<Biome>> original) {
        if (BiolithCompat.COMPAT_DATAGEN) {
            // During datagen we have to avoid adding registry keys.
            return original;
        }

        // Wrapping END.writeBiomeParameters() like this allows us to use the same interface there as we do for OVERWORLD.
        // So it looks kind of silly here, but it works fine and makes the code in the main biome placement classes alike.
        DynamicRegistryManager.Immutable registryManager = BiomeCoordinator.getRegistryManager();
        List<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameterList = new ArrayList<>(64);

        // Fallback lookup just in case.
        if (biolith$biomeLookup == null) {
            assert (registryManager != null);
            biolith$biomeLookup = registryManager.getWrapperOrThrow(RegistryKeys.BIOME);
        }

        // Generate "Vanilla" and modded parameters list.
        VanillaEndBiomeParameters.writeEndBiomeParameters(parameterList::add);
        BiomeCoordinator.END.writeBiomeParameters(parameterList::add);

        // Create a multi-noise parameter entries object.
        if (biolith$biomeEntries == null) {
            biolith$biomeEntries = new MultiNoiseUtil.Entries<>(parameterList.stream()
                    .map(pair -> pair.mapSecond((key) -> (RegistryEntry<Biome>) biolith$biomeLookup.getOrThrow(key)))
                    .toList());
        }

        // Output the registry entry stream (nominally the purpose of this method).
        return Streams.concat(
                original,
                parameterList.stream().map(pair -> biolith$biomeLookup.getOrThrow(pair.getSecond()))
            ).distinct();
    }

    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void biolith$getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // For the End we go to some lengths to mock up a multi-noise placement regime.
        // Still, we try to let other mods do whatever they do to place things in the End too.
        RegistryEntry<Biome> original = cir.getReturnValue();

        // Fake up a noise point for sub biome placement.
        MultiNoiseUtil.NoiseValuePoint noisePoint = ((EndBiomePlacement) BiomeCoordinator.END).sampleEndNoise(x, y, z, noise, original);

        // Select noise biome
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes = VanillaCompat.getEndBiome(noisePoint, biolith$biomeEntries, original);

        // Process any replacements or sub-biomes.
        cir.setReturnValue(BiomeCoordinator.END.getReplacement(x, y, z, noisePoint, fittestNodes));
    }

    @Override
    public MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$getBiomeEntries() {
        return biolith$biomeEntries;
    }
}