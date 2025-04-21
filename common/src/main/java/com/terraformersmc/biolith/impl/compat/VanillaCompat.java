package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.InterfaceSearchTree;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public class VanillaCompat {
    @SuppressWarnings("unchecked")
    // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
    public static BiolithFittestNodes<RegistryEntry<Biome>> getBiome(MultiNoiseUtil.NoiseValuePoint noisePoint, MultiNoiseUtil.Entries<RegistryEntry<Biome>> entries) {
        MultiNoiseUtil.SearchTree<RegistryEntry<Biome>> searchTree = entries.tree;

        return ((InterfaceSearchTree<RegistryEntry<Biome>>) (Object)searchTree).biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);
    }
}
