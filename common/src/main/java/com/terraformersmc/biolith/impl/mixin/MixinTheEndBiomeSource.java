package com.terraformersmc.biolith.impl.mixin;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.VanillaEndBiomeParameters;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.compat.VanillaCompat;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;

@Mixin(value = TheEndBiomeSource.class, priority = 990)
public abstract class MixinTheEndBiomeSource extends BiomeSource {
    @Unique
    private static HolderGetter<Biome> biolith$biomeLookup;
    @Unique
    private static Climate.ParameterList<Holder<Biome>> biolith$biomeEntries;

    @Inject(method = "create", at = @At("HEAD"))
    private static void biolith$getRegistry(HolderGetter<Biome> biomeLookup, CallbackInfoReturnable<TheEndBiomeSource> cir) {
        if (!biomeLookup.equals(biolith$biomeLookup)) {
            biolith$biomeLookup = biomeLookup;
            biolith$biomeEntries = null;
        }
    }

    @ModifyReturnValue(method = "collectPossibleBiomes", at = @At("RETURN"))
    @SuppressWarnings("unused")
    private Stream<Holder<Biome>> biolith$biomeStream(Stream<Holder<Biome>> original) {
        if (BiolithCompat.COMPAT_DATAGEN) {
            // During datagen we have to avoid adding registry keys.
            return original;
        }

        // Wrapping END.writeBiomeParameters() like this allows us to use the same interface there as we do for OVERWORLD.
        // So it looks kind of silly here, but it works fine and makes the code in the main biome placement classes alike.

        synchronized (this) {
            // Only compute this once, since our version is more expensive than Mojang's.
            if (biolith$biomeEntries == null) {
                List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameterList = new ArrayList<>(64);

                // Get an updated registry lookup if possible.
                BiomeCoordinator.getBiomeLookup().ifPresent(lookup -> biolith$biomeLookup = lookup);
                Objects.requireNonNull(biolith$biomeLookup, "Failed to acquire biome lookup for The End.");

                // Generate vanilla parameters list.
                VanillaEndBiomeParameters.writeEndBiomeParameters(parameterList::add);

                // Remove any biomes matching removals.
                parameterList.removeIf(entry ->
                        !BiomeCoordinator.END.removalFilter(entry.mapSecond((key) -> biolith$biomeLookup.getOrThrow(key))));

                // Add all biomes from additions, replacements, and sub-biome requests.
                BiomeCoordinator.END.writeBiomeParameters(parameterList::add);

                // Create a multi-noise parameter entries object.
                biolith$biomeEntries = new Climate.ParameterList<>(parameterList.stream()
                        .map(pair -> pair.mapSecond(key -> (Holder<Biome>) biolith$biomeLookup.getOrThrow(key)))
                        .toList());
            }
        } // synchronized (this)

        // Output the registry entry stream (nominally the purpose of this method).
        // Include the original entries in case another mod has appended to them.
        return Streams.concat(
                original,
                biolith$biomeEntries.values().stream().map(Pair::getSecond)
            ).distinct();
    }

    @ModifyReturnValue(method = "getNoiseBiome", at = @At("RETURN"))
    @SuppressWarnings("unused")
    private Holder<Biome> biolith$getBiome(Holder<Biome> original, int x, int y, int z, Climate.Sampler noise) {
        // For the End we go to some lengths to mock up a multi-noise placement regime.
        // Still, we try to let other mods do whatever they do to place things in the End too.

        // Fake up a noise point for sub biome placement.
        Climate.TargetPoint noisePoint = BiomeCoordinator.END.sampleEndNoise(x, y, z, noise, original);

        // Select noise biome
        BiolithFittestNodes<Holder<Biome>> fittestNodes = VanillaCompat.getEndBiome(noisePoint, this.biolith$getBiomeEntries(), original);

        // Process any replacements or sub-biomes.
        return BiomeCoordinator.END.getReplacement(x, y, z, noisePoint, fittestNodes);
    }

    @WrapOperation(method = "getNoiseBiome",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/world/level/levelgen/DensityFunction$SinglePointContext"
            )
    )
    @SuppressWarnings("unused")
    private DensityFunction.SinglePointContext biolith$smoothEndNoise(int blockX, int blockY, int blockZ, Operation<DensityFunction.SinglePointContext> original, int x, int y, int z) {
        return (new DensityFunction.SinglePointContext(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z)));
    }

    @Override
    public @NotNull Climate.ParameterList<Holder<Biome>> biolith$getBiomeEntries() {
        // I don't know why this hasn't always happened already, but sometimes it hasn't...
        if (biolith$biomeEntries == null) {
            this.collectPossibleBiomes();

            if (biolith$biomeEntries == null) {
                throw new IllegalStateException("biolith$biomeEntries is null after call to " + this.getClass().getCanonicalName() + ".biomeStream()");
            }
        }

        return biolith$biomeEntries;
    }
}
