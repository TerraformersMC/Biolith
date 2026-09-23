package com.terraformersmc.biolith.impl.surface;

import com.terraformersmc.biolith.api.surface.MaterialRuleBootstrapper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

import java.util.Arrays;

/*
 * Internal utility to facilitate sequencing wrapped material rules.
 */
public record SequencedMaterialRuleBootstrapper(MaterialRuleBootstrapper... children) implements MaterialRuleBootstrapper {
    @Override
    public MaterialRule apply(RegistryAccess registryAccess) {
        return MaterialRules.sequence(
                Arrays.stream(children)
                        .map(child -> child.apply(registryAccess))
                        .toArray(MaterialRule[]::new));
    }
}
