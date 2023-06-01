package com.terraformersmc.biolith.impl.mixin;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Mixin(targets = "net/minecraft/world/biome/source/MultiNoiseBiomeSourceParameterList$Preset$1")
public class MixinMultiNoiseParameterPresetNether {
    @ModifyReturnValue(method = "apply", at = @At("RETURN"))
    private <T> MultiNoiseUtil.Entries<T> biolith$applyNetherPreset(MultiNoiseUtil.Entries<T> original, Function<RegistryKey<Biome>, T> biomeEntryGetter) {
        // Wrapping NETHER.writeBiomeParameters() like this allows us to use the same interface there as we do for OVERWORLD.
        // So it looks kind of silly here, but it works fine and makes the code in the main biome placement classes alike.
        List<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameterList = new ArrayList<>(64);
        BiomeCoordinator.NETHER.writeBiomeParameters(parameterList::add);

        return new MultiNoiseUtil.Entries<>(
                Streams.concat(
                        original.getEntries().stream(),
                        parameterList.stream().map(pair -> Pair.of(pair.getFirst(), biomeEntryGetter.apply(pair.getSecond())))
                ).distinct().collect(Collectors.toList())
        );
    }
}
