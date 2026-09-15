package com.terraformersmc.biolith.impl.surface;

import com.terraformersmc.biolith.api.surface.MaterialRuleBootstrapper;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

import java.util.Arrays;

/*
 * Internal utility to facilitate sequencing wrapped material rules.
 */
public record SequencedMaterialRuleBootstrapper(MaterialRuleBootstrapper... children) implements MaterialRuleBootstrapper {
    @Override
    public MaterialRule apply(HolderGetter<Biome> biomeHolderGetter) {
        return MaterialRules.sequence(
                Arrays.stream(children)
                        .map(child -> child.apply(biomeHolderGetter))
                        .toArray(MaterialRule[]::new));
    }
}
