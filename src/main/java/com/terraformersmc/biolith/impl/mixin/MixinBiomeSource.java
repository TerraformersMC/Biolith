package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.biome.InterfaceBiomeSource;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BiomeSource.class)
public class MixinBiomeSource implements InterfaceBiomeSource {
    private RegistryEntry<DimensionType> biolith$dimensionTypeEntry;

    @Override
    public @Nullable RegistryEntry<DimensionType> biolith$getDimensionType() {
        return biolith$dimensionTypeEntry;
    }

    @Override
    public void biolith$setDimensionType(RegistryEntry<DimensionType> dimensionTypeEntry) {
        if (biolith$dimensionTypeEntry != null) {
            throw new IllegalStateException("Dimension Type already set: " + biolith$dimensionTypeEntry);
        }

        biolith$dimensionTypeEntry = dimensionTypeEntry;
    }
}
