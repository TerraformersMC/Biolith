package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import terrablender.api.Region;
import terrablender.api.SurfaceRuleManager;
import terrablender.worldgen.IExtendedParameterList;

import java.util.Map;

public class TerraBlenderCompatForge implements TerraBlenderCompat {
    @Override
    @SuppressWarnings("unchecked")
    // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
    public @Nullable BiolithFittestNodes<RegistryEntry<Biome>> getBiome(int x, int y, int z, MultiNoiseUtil.NoiseValuePoint noisePoint, MultiNoiseUtil.Entries<RegistryEntry<Biome>> biomeEntries) {
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes;

        // We have to hide this cast from the mixin; otherwise without TerraBlender it will crash during transformation...
        if (!(biomeEntries instanceof IExtendedParameterList<?> entries)) {
            // This method always terminates if the cast fails, so the pattern variable above remains in scope below.
            return null;
        }

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

    @Override
    public void registerSurfaceRules() {
        Map.of(
                SurfaceRuleCollector.OVERWORLD, SurfaceRuleManager.RuleCategory.OVERWORLD,
                SurfaceRuleCollector.NETHER,    SurfaceRuleManager.RuleCategory.NETHER
        ).forEach((biolithRules, terrablenderRuleCategory) -> {
            if (biolithRules.getRuleCount() > 0) {
                for (Identifier ruleOwner : biolithRules.getRuleOwners()) {
                    if (biolithRules.getRuleCount(ruleOwner) > 0) {
                        if (ruleOwner.getNamespace().equals("minecraft")) {
                            Biolith.LOGGER.warn("Unable to modify surface rules of vanilla biomes via TerraBlender; dropping: {}", ruleOwner);
                            continue;
                        }
                        try {
                            SurfaceRuleManager.addSurfaceRules(
                                    terrablenderRuleCategory,
                                    ruleOwner.getNamespace(),
                                    biolithRules.get(ruleOwner)
                            );
                        } catch(IllegalArgumentException e) {
                            Biolith.LOGGER.debug("Exception: {}", e.getMessage());
                            Biolith.LOGGER.warn("Only one surface rule set per namespace can be registered with TerraBlender; dropping: {}", ruleOwner);
                        }
                    }
                }
            }
        });
    }
}