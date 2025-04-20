package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import terrablender.api.Region;
import terrablender.worldgen.IExtendedParameterList;

public interface TerraBlenderCompat {
    @Nullable BiolithFittestNodes<RegistryEntry<Biome>> getBiome(int x, int y, int z, MultiNoiseUtil.NoiseValuePoint noisePoint, MultiNoiseUtil.Entries<RegistryEntry<Biome>> biomeEntries);

    void registerSurfaceRules();
}
