package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public class VanillaCompat {
    @SuppressWarnings("unchecked")
    // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
    public static BiolithFittestNodes<RegistryEntry<Biome>> getBiome(MultiNoiseUtil.NoiseValuePoint noisePoint, MultiNoiseUtil.Entries<RegistryEntry<Biome>> entries) {
        return entries.tree.biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);
    }
}
