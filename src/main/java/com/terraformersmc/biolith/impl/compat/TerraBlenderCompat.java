package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import terrablender.api.Region;
import terrablender.worldgen.IExtendedParameterList;

public class TerraBlenderCompat {
    @SuppressWarnings("unchecked")
    // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
    public static @Nullable BiolithFittestNodes<RegistryEntry<Biome>> getBiome(int x, int y, int z, MultiNoiseUtil.NoiseValuePoint noisePoint, IExtendedParameterList<?> entries) {
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes;

        // Fall back to Vanilla if TerraBlender thinks it is not ready.
        if (!entries.isInitialized()) {
            return null;
        }

        // Get TerraBlender's Region-specific search tree for the (x,z) coordinates.
        MultiNoiseUtil.SearchTree<RegistryEntry<Biome>> searchTree = entries.getTree(entries.getUniqueness(x, y, z));

        // Fall back to Vanilla if TerraBlender has no SearchTree.
        if (searchTree == null) {
            return null;
        }

        // Apply our RTree search implementation to TerraBlender's search tree.
        fittestNodes = searchTree.biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);

        // TerraBlender requires a second search if the first returned their placeholder biome.
        if (fittestNodes.ultimate().value.matchesKey(Region.DEFERRED_PLACEHOLDER)) {
            searchTree = entries.getTree(0);
            fittestNodes = searchTree.biolith$searchTreeGet(noisePoint, MultiNoiseUtil.SearchTree.TreeNode::getSquaredDistance);
        }

        return fittestNodes;
    }
}
