package com.terraformersmc.biolith.impl.biome;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface InterfaceBiomeSource {
    RegistryEntry<DimensionType> biolith$getDimensionType();

    void biolith$setDimensionType(RegistryEntry<DimensionType> dimensionTypeEntry);

    default @Nullable MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$getBiomeEntries() {
        return null;
    }

    default boolean biolith$getBypass() {
        return false;
    }

    default void biolith$setBypass(boolean value) {
    }
}
