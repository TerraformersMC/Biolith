package com.terraformersmc.biolith.api.surface;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

import java.util.function.Function;

/**
 * Functional interface enabling deferred initialization of material rules.  Mods should provide
 * an implementing function which uses the provided biome holder getter to return instantiated
 * MaterialRule objects.  (As of 26.2, {@linkplain MaterialRules#isBiome} requires the biome registry
 * and is the only MaterialRule which requires registry access to instantiate.)
 * <p/>
 * This can be an implementing class which overrides {@linkplain #apply}, but the minimal
 * implementation is something like this:
 * <pre>{@code biomeGetter -> ifTrue(MaterialRules.isBiome(biomeGetter, Biomes.PLAINS), ...)}</pre>
 */
public interface MaterialRuleBootstrapper extends Function<HolderGetter<Biome>, MaterialRule> {
    /**
     * You can use this method to promote an implementing function to this interface in
     * situations where the compiler is unable to properly infer the cast on its own.
     *
     * @param function An implementing (perhaps anonymous) function
     * @return The implementing function cast to MaterialRuleBootstrapper
     */
    static MaterialRuleBootstrapper cast(Function<HolderGetter<Biome>, MaterialRule> function) {
        return function::apply;
    }
}
