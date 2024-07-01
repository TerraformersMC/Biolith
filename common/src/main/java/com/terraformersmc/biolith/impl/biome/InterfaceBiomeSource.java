package com.terraformersmc.biolith.impl.biome;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface InterfaceBiomeSource {
    RegistryEntry<DimensionType> biolith$getDimensionType();

    void biolith$setDimensionType(RegistryEntry<DimensionType> dimensionTypeEntry);

    default @NotNull MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$getBiomeEntries() {
        throw new UnsupportedOperationException("Unimplemented on abstract BiomeSource!");
    }

    default boolean biolith$getBypass() {
        return false;
    }

    default void biolith$setBypass(boolean value) {
    }
}
