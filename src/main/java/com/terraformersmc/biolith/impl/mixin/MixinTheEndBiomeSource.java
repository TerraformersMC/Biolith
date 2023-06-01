package com.terraformersmc.biolith.impl.mixin;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.EndBiomePlacement;
import com.terraformersmc.biolith.impl.biome.VanillaEndBiomeParameters;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@Mixin(value = TheEndBiomeSource.class, priority = 900)
public abstract class MixinTheEndBiomeSource extends BiomeSource {
    private static RegistryEntryLookup<Biome> biolith$biomeLookup;
    private static MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$biomeEntries;

    @Inject(method = "createVanilla", at = @At("HEAD"))
    private static void biolith$getRegistry(RegistryEntryLookup<Biome> biomeLookup, CallbackInfoReturnable<TheEndBiomeSource> cir) {
        biolith$biomeLookup = biomeLookup;
    }

    @ModifyReturnValue(method = "biomeStream", at = @At("RETURN"))
    private Stream<RegistryEntry<Biome>> biolith$biomeStream(Stream<RegistryEntry<Biome>> original) {
        // Wrapping END.writeBiomeParameters() like this allows us to use the same interface there as we do for OVERWORLD.
        // So it looks kind of silly here, but it works fine and makes the code in the main biome placement classes alike.
        DynamicRegistryManager.Immutable registryManager = BiomeCoordinator.getRegistryManager();
        List<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameterList = new ArrayList<>(64);

        // Fallback lookup just in case.
        if (biolith$biomeLookup == null) {
            assert (registryManager != null);
            RegistryWrapper<Biome> biomeWrapper;
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
        DynamicRegistryManager.Immutable registryManager = BiomeCoordinator.getRegistryManager();
        RegistryEntry<Biome> original = cir.getReturnValue();

        // Fake up a noise point for sub biome placement.
        EndBiomePlacement biomePlacement = (EndBiomePlacement) BiomeCoordinator.END;
        double erosion = noise.erosion().sample(new DensityFunction.UnblendedNoisePos(
                (ChunkSectionPos.getSectionCoord(BiomeCoords.toBlock(x)) * 2 + 1) * 8,
                BiomeCoords.toBlock(y),
                (ChunkSectionPos.getSectionCoord(BiomeCoords.toBlock(z)) * 2 + 1) * 8
        ));
        MultiNoiseUtil.NoiseValuePoint noisePoint = new MultiNoiseUtil.NoiseValuePoint(
                MultiNoiseUtil.toLong(biomePlacement.temperatureNoise.sample(x / 576d, z / 576d)),
                MultiNoiseUtil.toLong(biomePlacement.humidityNoise.sample(x / 448d, z / 448d)),
                original.matchesKey(BiomeKeys.THE_END) ? 0L : MultiNoiseUtil.toLong(MathHelper.clamp((float) erosion + 0.0625f, -1f, 1f)),
                MultiNoiseUtil.toLong((float) erosion),
                156L * (56 - y),
                MultiNoiseUtil.toLong(biomePlacement.weirdnessNoise.sample(x / 192d, z / 192d))
        );

        // Select noise biome
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes;
        if (original.matchesKey(BiomeKeys.THE_END)) {
            // We do not use noise to replace the central End biome; replacements must be explicit.
            // As such, there is no second-best-fit biome.
            MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> ultimate = new MultiNoiseUtil.SearchTree.TreeLeafNode<>(
                    new MultiNoiseUtil.NoiseHypercube(
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.temperatureNoise())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.humidityNoise())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.continentalnessNoise())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.erosionNoise())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.depth())),
                            MultiNoiseUtil.ParameterRange.of(MultiNoiseUtil.toFloat(noisePoint.weirdnessNoise())),
                            0L),
                    original);
            fittestNodes = new BiolithFittestNodes<>(ultimate, 0);
        } else {
            // Evaluate the best fit biome by noise at the noise point.
            // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
            //noinspection unchecked
            fittestNodes = biolith$biomeEntries.tree.biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);
        }

        // If the best noise fit was a vanilla biome, let whatever vanilla picked leak through.
        // This way if other mods have directly modified vanilla biome selection, it may still work.
        if (!original.equals(fittestNodes.ultimate().value) && (
                fittestNodes.ultimate().value.matchesKey(BiomeKeys.SMALL_END_ISLANDS) ||
                fittestNodes.ultimate().value.matchesKey(BiomeKeys.END_BARRENS) ||
                fittestNodes.ultimate().value.matchesKey(BiomeKeys.END_MIDLANDS) ||
                fittestNodes.ultimate().value.matchesKey(BiomeKeys.END_HIGHLANDS))) {

            fittestNodes = new BiolithFittestNodes<>(
                    new MultiNoiseUtil.SearchTree.TreeLeafNode<>(biolith$createNoiseHypercube(fittestNodes.ultimate().parameters), original),
                    0L,
                    fittestNodes.ultimate(),
                    fittestNodes.ultimateDistance()
            );
        }

        // Process any replacements or sub-biomes.
        cir.setReturnValue(BiomeCoordinator.END.getReplacement(x, y, z, noisePoint, fittestNodes));
    }

    private static MultiNoiseUtil.NoiseHypercube biolith$createNoiseHypercube(MultiNoiseUtil.ParameterRange... parameters) {
        assert parameters.length == 6;
        return MultiNoiseUtil.createNoiseHypercube(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], parameters[5], 0L);
    }
}
