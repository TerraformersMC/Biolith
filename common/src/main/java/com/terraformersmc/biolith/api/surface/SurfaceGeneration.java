package com.terraformersmc.biolith.api.surface;

import com.terraformersmc.biolith.impl.surface.SurfaceBuilderCollector;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Code API methods for surface generation strategies:
 *
 * <ul>
 * <li>{@linkplain #addEndSurfaceRules} - Prepend surface rules for End biomes</li>
 * <li>{@linkplain #addNetherSurfaceRules} - Prepend surface rules for Nether biomes</li>
 * <li>{@linkplain #addOverworldSurfaceRules} - Prepend surface rules for Overworld biomes</li>
 * <li>{@linkplain #addSurfaceBuilder} - Add a traditional surface builder (selected by biome)</li>
 * </ul>
 */
@SuppressWarnings("unused")
public final class SurfaceGeneration {
    private SurfaceGeneration() {
        throw new UnsupportedOperationException();
    }

    /**
     * Add surface rules to The End.  Rules may optionally be pre-sequenced,
     * or Biolith will sequence rules together grouped by rulesOwner, prior to injection.
     * <p/>
     * Rule instantiation since 26.2 requires registry access; rules must be provided wrapped in an
     * implementation of the RuleSourceBootstrapper interface.  The minimum implementation is something
     * like this:
     * <pre>{@code biomeGetter -> ifTrue(SurfaceRules.isBiome(biomeGetter, Biomes.PLAINS), ...)}</pre>
     * <p/>
     * Note: The End's only vanilla rule is {@code block(Blocks.END_STONE.getDefaultState()) }
     *
     * @param rulesOwner    Rules will be grouped by rulesOwner during sequencing
     * @param materialRules The surface rules to be injected
     */
    public static void addEndSurfaceRules(Identifier rulesOwner, RuleSourceBootstrapper... materialRules) {
        SurfaceRuleCollector.END.addFromMods(rulesOwner, materialRules);
    }

    /**
     * Remove all surface rules owned by rulesOwner from The End.
     *
     * @param rulesOwner Rules with the specified owner will be removed
     * @return A list containing the removed rules, or null if there were none
     */
    public static @Nullable List<RuleSourceBootstrapper> removeEndSurfaceRules(Identifier rulesOwner) {
        return SurfaceRuleCollector.END.clearFromMod(rulesOwner);
    }

    /**
     * Add surface rules to The Nether.  Rules may optionally be pre-sequenced,
     * or Biolith will sequence rules together grouped by rulesOwner, prior to injection.
     * <p/>
     * Rule instantiation since 26.2 requires registry access; rules must be provided wrapped in an
     * implementation of the RuleSourceBootstrapper interface.  The minimum implementation is something
     * like this:
     * <pre>{@code biomeGetter -> ifTrue(SurfaceRules.isBiome(biomeGetter, Biomes.PLAINS), ...)}</pre>
     * <p/>
     * For TerraBlender compatibility, it is important the rulesOwner's namespace
     * should be the identical to the namespace of all biomes to which the rules apply.
     *
     * @param rulesOwner    Rules will be grouped by rulesOwner during sequencing
     * @param materialRules The surface rules to be injected
     */
    public static void addNetherSurfaceRules(Identifier rulesOwner, RuleSourceBootstrapper... materialRules) {
        SurfaceRuleCollector.NETHER.addFromMods(rulesOwner, materialRules);
    }

    /**
     * Remove all surface rules owned by rulesOwner from The Nether.
     *
     * @param rulesOwner Rules with the specified owner will be removed
     * @return A list containing the removed rules, or null if there were none
     */
    public static @Nullable List<RuleSourceBootstrapper> removeNetherSurfaceRules(Identifier rulesOwner) {
        return SurfaceRuleCollector.NETHER.clearFromMod(rulesOwner);
    }

    /**
     * Add surface rules to the Overworld.  Rules may optionally be pre-sequenced,
     * or Biolith will sequence rules together grouped by rulesOwner, prior to injection.
     * <p/>
     * Rule instantiation since 26.2 requires registry access; rules must be provided wrapped in an
     * implementation of the RuleSourceBootstrapper interface.  The minimum implementation is something
     * like this:
     * <pre>{@code biomeGetter -> ifTrue(SurfaceRules.isBiome(biomeGetter, Biomes.PLAINS), ...)}</pre>
     * <p/>
     * For TerraBlender compatibility, it is important the rulesOwner's namespace
     * should be the identical to the namespace of all biomes to which the rules apply.
     *
     * @param rulesOwner    Rules will be grouped by rulesOwner during sequencing
     * @param materialRules The rules to be injected
     */
    public static void addOverworldSurfaceRules(Identifier rulesOwner, RuleSourceBootstrapper... materialRules) {
        SurfaceRuleCollector.OVERWORLD.addFromMods(rulesOwner, materialRules);
    }

    /**
     * Remove all surface rules owned by rulesOwner from the Overworld.
     *
     * @param rulesOwner Rules with the specified owner will be removed
     * @return A list containing the removed rules, or null if there were none
     */
    public static @Nullable List<RuleSourceBootstrapper> removeOverworldSurfaceRules(Identifier rulesOwner) {
        return SurfaceRuleCollector.OVERWORLD.clearFromMod(rulesOwner);
    }


    /**
     * Add a surface builder to all dimensions built by Minecraft's SurfaceBuilder.buildSurface() method.
     * The surface builder should extend {@link BiolithSurfaceBuilder} and further documentation is available in
     * the interface.
     *
     * @param builderId      A unique Identifier for the surface builder
     * @param surfaceBuilder The surface builder to be injected
     */
    public static void addSurfaceBuilder(Identifier builderId, BiolithSurfaceBuilder surfaceBuilder) {
        SurfaceBuilderCollector.add(surfaceBuilder);
    }
}
