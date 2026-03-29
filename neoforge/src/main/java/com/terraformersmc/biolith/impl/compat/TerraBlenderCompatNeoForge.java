package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jspecify.annotations.Nullable;
import terrablender.api.Region;
import terrablender.api.SurfaceRuleManager;
import terrablender.worldgen.IExtendedParameterList;

import java.util.Map;

public class TerraBlenderCompatNeoForge implements TerraBlenderCompat {
    @Override
    @SuppressWarnings("unchecked")
    // Unchecked because of parameterized types (which are always Holder<Biome>)
    public @Nullable BiolithFittestNodes<Holder<Biome>> getBiome(int x, int y, int z, Climate.TargetPoint noisePoint, Climate.ParameterList<Holder<Biome>> biomeEntries) {
        BiolithFittestNodes<Holder<Biome>> fittestNodes;

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
        Climate.RTree<Holder<Biome>> searchTree = entries.getTree(entries.getUniqueness(x, y, z));

        // Fall back to Vanilla if TerraBlender has no SearchTree.
        if (searchTree == null) {
            return null;
        }

        // Apply our RTree search implementation to TerraBlender's search tree.
        fittestNodes = searchTree.biolith$searchTreeGet(noisePoint, Climate.RTree.Node::distance);

        // TerraBlender requires a second search if the first returned their placeholder biome.
        if (fittestNodes.ultimate().value.is(Region.DEFERRED_PLACEHOLDER)) {
            searchTree = entries.getTree(0);
            fittestNodes = searchTree.biolith$searchTreeGet(noisePoint, Climate.RTree.Node::distance);
        }

        return fittestNodes;
    }

    @Override
    public void registerSurfaceRules() {
        Map.of(
                SurfaceRuleCollector.OVERWORLD, SurfaceRuleManager.RuleCategory.OVERWORLD,
                SurfaceRuleCollector.NETHER,    SurfaceRuleManager.RuleCategory.NETHER,
                SurfaceRuleCollector.END,       SurfaceRuleManager.RuleCategory.END
        ).forEach((biolithRules, terrablenderRuleCategory) -> {
            if (biolithRules.getRuleCount() > 0) {
                for (Identifier ruleOwner : biolithRules.getRuleOwners()) {
                    String namespace = ruleOwner.getNamespace();
                    SurfaceRules.RuleSource rule = biolithRules.get(ruleOwner);
                    if (rule != null) {
                        if (namespace.equals("minecraft")) {
                            SurfaceRuleManager.addToDefaultSurfaceRulesAtStage(terrablenderRuleCategory, SurfaceRuleManager.RuleStage.BEFORE_BEDROCK, 0, rule);
                            continue;
                        }
                        try {
                            SurfaceRuleManager.addSurfaceRules(terrablenderRuleCategory, namespace, rule);
                        } catch (IllegalArgumentException e) {
                            Biolith.LOGGER.debug("Exception: {}", e.getMessage());
                            Biolith.LOGGER.warn("Only one surface rule set per namespace can be registered with TerraBlender; dropping: {}", ruleOwner);
                        }
                    }
                }
            }
        });
    }
}
