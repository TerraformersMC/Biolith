package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

public interface TerraBlenderCompat {
    @Nullable BiolithFittestNodes<Holder<Biome>> getBiome(int x, int y, int z, Climate.TargetPoint noisePoint, Climate.ParameterList<Holder<Biome>> biomeEntries);

    void registerSurfaceRules();
}
