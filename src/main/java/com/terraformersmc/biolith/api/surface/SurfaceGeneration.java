package com.terraformersmc.biolith.api.surface;

import com.terraformersmc.biolith.impl.surface.SurfaceBuilderCollector;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

@SuppressWarnings("unused")
public class SurfaceGeneration {
    /**
     * SurfaceGeneration.addOverworldSurfaceRules()
     *
     * Add surface rules to the Overworld.  Rules may optionally be pre-sequenced,
     * or Biolith will sequence rules together grouped by rulesOwner, prior to injection.
     *
     * @param rulesOwner Identifier - Rules will be grouped by rulesOwner during sequencing
     * @param materialRules MaterialRules.MaterialRule - The rules to be injected
     */
    public static void addOverworldSurfaceRules(Identifier rulesOwner, MaterialRules.MaterialRule... materialRules) {
        SurfaceRuleCollector.OVERWORLD.add(rulesOwner, materialRules);
    }

    /**
     * SurfaceGeneration.addNetherSurfaceRules()
     *
     * Add surface rules to The Nether.  Rules may optionally be pre-sequenced,
     * or Biolith will sequence rules together grouped by rulesOwner, prior to injection.
     *
     * @param rulesOwner Identifier - Rules will be grouped by rulesOwner during sequencing
     * @param materialRules MaterialRules.MaterialRule - The rules to be injected
     */
    public static void addNetherSurfaceRules(Identifier rulesOwner, MaterialRules.MaterialRule... materialRules) {
        SurfaceRuleCollector.NETHER.add(rulesOwner, materialRules);
    }

    /**
     * SurfaceGeneration.addSurfaceBuilder()
     *
     * Add a surface builder to all dimensions built by Minecraft's SurfaceBuilder.buildSurface() method.
     * The surface builder should extend BiolithSurfaceBuilder and further documentation is available in
     * the interface.
     *
     * @param builderId Identifier - A unique Identifier for the surface builder
     * @param surfaceBuilder extends BiolithSurfaceBuilder - The surface builder to be injected
     */
    public static void addSurfaceBuilder(Identifier builderId, BiolithSurfaceBuilder surfaceBuilder) {
        SurfaceBuilderCollector.add(surfaceBuilder);
    }
}
