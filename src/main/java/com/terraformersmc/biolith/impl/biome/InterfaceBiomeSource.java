package com.terraformersmc.biolith.impl.biome;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;

@SuppressWarnings("unused")
public interface InterfaceBiomeSource {
    RegistryEntry<DimensionType> biolith$getDimensionType();

    void biolith$setDimensionType(RegistryEntry<DimensionType> dimensionTypeEntry);
}