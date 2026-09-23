package com.terraformersmc.biolith.api.surface;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.levelgen.material.MaterialRules;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;

import java.util.function.Function;

/**
 * Functional interface enabling deferred initialization of material rules.  Mods should provide
 * an implementing function which uses the provided registry access to return instantiated
 * MaterialRule objects.  (As of 26.2, {@linkplain MaterialRules#isBiome} requires the biome registry.
 * As of 26.3, the material conditions registry is also required to use vanilla material conditions.)
 * <p/>
 * This can be an implementing class which overrides {@linkplain #apply}, but the minimal
 * implementation is something like this:
 * <pre>{@code registryAccess -> ifTrue(MaterialRules.isBiome(
 *      registryAccess.lookupOrThrow(Registries.BIOME), Biomes.PLAINS), ...)}</pre>
 */
public interface MaterialRuleBootstrapper extends Function<RegistryAccess, MaterialRule> {
    /**
     * You can use this method to promote an implementing function to this interface in
     * situations where the compiler is unable to properly infer the cast on its own.
     *
     * @param function An implementing (perhaps anonymous) function
     * @return The implementing function cast to MaterialRuleBootstrapper
     */
    static MaterialRuleBootstrapper cast(Function<RegistryAccess, MaterialRule> function) {
        return function::apply;
    }
}
