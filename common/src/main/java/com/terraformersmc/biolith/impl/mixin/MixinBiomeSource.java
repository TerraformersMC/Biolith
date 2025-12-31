package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.InterfaceBiomeSource;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BiomeSource.class)
public class MixinBiomeSource implements InterfaceBiomeSource {
    @Unique
    private ResourceKey<DimensionType> biolith$dimensionType = InterfaceBiomeSource.DIMENSION_TYPE_UNDEFINED;

    @Override
    public ResourceKey<DimensionType> biolith$getDimensionType() {
        return biolith$dimensionType;
    }

    @Override
    public void biolith$setDimensionType(Holder<DimensionType> dimensionTypeEntry) {
        dimensionTypeEntry.unwrapKey().ifPresent(this::biolith$setDimensionType);
    }

    @Override
    public void biolith$setDimensionType(ResourceKey<DimensionType> dimensionTypeKey) {
        if (!biolith$dimensionType.identifier().equals(InterfaceBiomeSource.DIMENSION_TYPE_UNDEFINED.identifier()) &&
                !biolith$dimensionType.identifier().equals(dimensionTypeKey.identifier())) {
            Biolith.LOGGER.warn("Dimension Type modified: from '{}' to '{}'",
                    biolith$dimensionType.identifier(), dimensionTypeKey.identifier());
        }

        biolith$dimensionType = dimensionTypeKey;
    }
}
