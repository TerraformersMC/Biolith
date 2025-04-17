package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface InterfaceBiomeSource {
    RegistryKey<DimensionType> DIMENSION_TYPE_UNDEFINED =
            RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of(Biolith.MOD_ID, "undefined"));

    RegistryKey<DimensionType> biolith$getDimensionType();

    void biolith$setDimensionType(RegistryEntry<DimensionType> dimensionTypeEntry);

    void biolith$setDimensionType(RegistryKey<DimensionType> dimensionTypeEntry);

    default @Nullable MultiNoiseUtil.Entries<RegistryEntry<Biome>> biolith$getBiomeEntries() {
        return null;
    }

    default boolean biolith$getBypass() {
        return false;
    }

    default void biolith$setBypass(boolean value) {
    }
}