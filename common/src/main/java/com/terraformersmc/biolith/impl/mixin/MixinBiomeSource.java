package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.InterfaceBiomeSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BiomeSource.class)
public class MixinBiomeSource implements InterfaceBiomeSource {
    @Unique
    private RegistryKey<DimensionType> biolith$dimensionType = InterfaceBiomeSource.DIMENSION_TYPE_UNDEFINED;

    @Override
    public RegistryKey<DimensionType> biolith$getDimensionType() {
        return biolith$dimensionType;
    }

    @Override
    public void biolith$setDimensionType(RegistryEntry<DimensionType> dimensionTypeEntry) {
        dimensionTypeEntry.getKey().ifPresent(this::biolith$setDimensionType);
    }

    @Override
    public void biolith$setDimensionType(RegistryKey<DimensionType> dimensionTypeKey) {
        if (!biolith$dimensionType.getValue().equals(InterfaceBiomeSource.DIMENSION_TYPE_UNDEFINED.getValue()) &&
                !biolith$dimensionType.getValue().equals(dimensionTypeKey.getValue())) {
            Biolith.LOGGER.warn("Dimension Type modified: from '{}' to '{}'",
                    biolith$dimensionType.getValue(), dimensionTypeKey.getValue());
        }

        biolith$dimensionType = dimensionTypeKey;
    }
}
