package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

@SuppressWarnings("unused")
public interface InterfaceBiomeSource {
    RegistryKey<DimensionType> DIMENSION_TYPE_UNDEFINED =
            RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of(Biolith.MOD_ID, "undefined"));

    RegistryKey<DimensionType> biolith$getDimensionType();

    void biolith$setDimensionType(RegistryEntry<DimensionType> dimensionTypeEntry);

    void biolith$setDimensionType(RegistryKey<DimensionType> dimensionTypeEntry);
}