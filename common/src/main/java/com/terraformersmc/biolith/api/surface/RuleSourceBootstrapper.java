package com.terraformersmc.biolith.api.surface;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;

import java.util.function.Function;

/**
 * Functional interface enabling deferred initialization of surface rules.  Mods should provide
 * an implementing function which uses the provided biome holder getter to return instantiated
 * RuleSource objects.  (As of 26.2, {@linkplain SurfaceRules#isBiome} requires the biome registry
 * and is the only RuleSource which requires registry access to instantiate.)
 * <p/>
 * This can be an implementing class which overrides {@linkplain #apply}, but the minimal
 * implementation is something like this:
 * <pre>{@code biomeGetter -> ifTrue(SurfaceRules.isBiome(biomeGetter, Biomes.PLAINS), ...)}</pre>
 */
public interface RuleSourceBootstrapper extends Function<HolderGetter<Biome>, SurfaceRules.RuleSource> {
    /**
     * You can use this method to promote an implementing function to this interface in
     * situations where the compiler is unable to properly infer the cast on its own.
     *
     * @param function An implementing (perhaps anonymous) function
     * @return The implementing function cast to RuleSourceBootstrapper
     */
    static RuleSourceBootstrapper cast(Function<HolderGetter<Biome>, SurfaceRules.RuleSource> function) {
        return function::apply;
    }
}
