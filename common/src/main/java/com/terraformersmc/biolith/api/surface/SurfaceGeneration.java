package com.terraformersmc.biolith.api.surface;

import com.terraformersmc.biolith.impl.surface.SurfaceBuilderCollector;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.SurfaceRules;

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
     * <p></p>
     * Note: The End's only vanilla rule is {@code block(Blocks.END_STONE.getDefaultState()) }
     *
     * @param rulesOwner    Rules will be grouped by rulesOwner during sequencing
     * @param materialRules The surface rules to be injected
     */
    public static void addEndSurfaceRules(Identifier rulesOwner, SurfaceRules.RuleSource... materialRules) {
        SurfaceRuleCollector.END.addFromMods(rulesOwner, materialRules);
    }

    /**
     * Add surface rules to The Nether.  Rules may optionally be pre-sequenced,
     * or Biolith will sequence rules together grouped by rulesOwner, prior to injection.
     * <p></p>
     * For TerraBlender compatibility, it is important the rulesOwner's namespace
     * should be the identical to the namespace of all biomes to which the rules apply.
     *
     * @param rulesOwner    Rules will be grouped by rulesOwner during sequencing
     * @param materialRules The surface rules to be injected
     */
    public static void addNetherSurfaceRules(Identifier rulesOwner, SurfaceRules.RuleSource... materialRules) {
        SurfaceRuleCollector.NETHER.addFromMods(rulesOwner, materialRules);
    }

    /**
     * Add surface rules to the Overworld.  Rules may optionally be pre-sequenced,
     * or Biolith will sequence rules together grouped by rulesOwner, prior to injection.
     * <p></p>
     * For TerraBlender compatibility, it is important the rulesOwner's namespace
     * should be the identical to the namespace of all biomes to which the rules apply.
     *
     * @param rulesOwner    Rules will be grouped by rulesOwner during sequencing
     * @param materialRules The rules to be injected
     */
    public static void addOverworldSurfaceRules(Identifier rulesOwner, SurfaceRules.RuleSource... materialRules) {
        SurfaceRuleCollector.OVERWORLD.addFromMods(rulesOwner, materialRules);
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
