package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.DimensionType;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public interface InterfaceBiomeSource {
    ResourceKey<DimensionType> DIMENSION_TYPE_UNDEFINED =
            ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(Biolith.MOD_ID, "undefined"));

    ResourceKey<DimensionType> biolith$getDimensionType();

    void biolith$setDimensionType(Holder<DimensionType> dimensionTypeEntry);

    void biolith$setDimensionType(ResourceKey<DimensionType> dimensionTypeEntry);

    default Climate.@Nullable ParameterList<Holder<Biome>> biolith$getBiomeEntries() {
        return null;
    }

    default boolean biolith$getBypass() {
        return false;
    }

    default void biolith$setBypass(boolean value) {
    }

    // Used to extend injected interface coverage to the forges
    static InterfaceBiomeSource cast(BiomeSource biomeSource) {
        return biomeSource;
    }
}
