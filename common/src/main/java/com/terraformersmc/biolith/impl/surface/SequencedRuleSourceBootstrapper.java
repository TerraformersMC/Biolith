package com.terraformersmc.biolith.impl.surface;

import com.terraformersmc.biolith.api.surface.RuleSourceBootstrapper;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.Arrays;

/*
 * Internal utility to facilitate sequencing wrapped rule sources.
 */
public record SequencedRuleSourceBootstrapper(RuleSourceBootstrapper... children) implements RuleSourceBootstrapper {
    @Override
    public SurfaceRules.RuleSource apply(HolderGetter<Biome> biomeHolderGetter) {
        return SurfaceRules.sequence(
                Arrays.stream(children)
                        .map(child -> child.apply(biomeHolderGetter))
                        .toArray(SurfaceRules.RuleSource[]::new));
    }
}
