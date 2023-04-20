<p align="center"><a href="https://modrinth.com/mod/biolith"><img height="286" width="286" src="./images/biolith.png" /></a></p>

# Biolith
A biome placement mod focusing on configurability and consistent
distribution of modded biomes

## Warning: EXPERIMENTAL

This mod is still somewhat experimental.  APIs will remain unstable until
the first beta release of Biolith.  Biome selection strategies may change,
resulting in biomes being placed at different locations in existing worlds.

## Extent of Current Features

At this time, aside from any new things I've implemented and forgotten
to update here, the following features are present:

* Place an Overworld or Nether biome at a specified noise point.
* Replace an Overworld or Nether biome entirely or in part.
* Add a sub-biome to an Overworld or Nether or modded biome.

* Register custom surface rules.
* Override vanilla surfaces using custom surface builders.

Biolith 0.0.1 alpha releases are known to be compatible with Fabric Minecraft 1.19.4 and 1.20 snapshots.

## Releases via Maven

Much like Terraform API, add the Terraformers maven repository to your `build.gradle`:

```
repositories {
    maven {
        name = 'TerraformersMC'
        url = 'https://maven.terraformersmc.com/'
    }
}
```

And add the mod to the dependencies section of `build.gradle`:

```
dependencies {
    modImplementation("com.terraformersmc:biolith:${project.biolith_version}")
}
```

If you wish to include Biolith in your mod for distribution, wrap the `modImplementation()` with an `include()`

Finally, set the Biolith version you want in `gradle.properties`:

```
biolith_version=0.0.1-alpha.6
```

## Examples

A wiki would be nice.  For now, here's some illegible probably outdated examples instead.

```java
public void during_mod_init() {
    // Place a biome at a specific noise point
    BiomePlacement.addNether(ModBiomeKeys.LUMINOUS_GROVE, MultiNoiseUtil.createNoiseHypercube(0.35F, 0.3F, 0.0F, 0.0F, 0.0F, 0.0F, 0.225F)); }

    // replace a vanilla biome 20% of the time
    BiomePlacement.replaceOverworld(BiomeKeys.FOREST, ModBiomeKeys.AUTUMNAL_WOODS, 0.2D);

    // add an edge sub-biome
    BiomePlacement.addSubOverworld(BiomeKeys.DESERT, ModBiomeKeys.OASIS, SubBiomeMatcher.of(SubBiomeMatcher.NEAR_BORDER));

    // add a sub-biome based on noise
    BiomePlacement.addSubOverworld(ModBiomeKeys.LUSH_DESERT, ModBiomeKeys.OASIS, SubBiomeMatcher.of(SubBiomeMatcher.Criterion.ofMax(SubBiomeMatcher.CriterionTargets.PEAKS_VALLEYS, SubBiomeMatcher.CriterionTypes.DISTANCE, 0.2f)));

    // register surface rule(s)
    SurfaceGeneration.addOverworldSurfaceRules(Identifier.of(Mod.MOD_ID, "surface_rules"), modSurfaceRules);

    // use surface builders
    // See the javadocs for this; surface builders are a complicated topic.
    // See also: [Terrestria's surface builders](https://github.com/TerraformersMC/Terrestria/tree/1.19.3/worldgen/src/main/java/com/terraformersmc/terrestria/surfacebuilders)
}
```