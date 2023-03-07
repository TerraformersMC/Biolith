![icon](./src/main/resources/assets/biolith/icon.png)

# Biolith
A biome placement mod focusing on configurability and consistent
distribution of modded biomes

## Warning: EXPERIMENTAL

This mod is an experiment.  It may change at any time.  There is no such
thing as a stable API.  There are missing features.  It may eat your
favorite socks.  It may corrupt your favorite world.  Any version you
find or do not find may be the last version ever made.  I could abandon
it and join a monastery.  YOU HAVE BEEN WARNED.

There is no maven repository yet.  You have to download
[a GH alpha release](https://github.com/gniftygnome/Biolith/releases).

## Extent of Current Features

At this time, aside from any new things I've implemented and forgotten
to update here, the following features are present:

* Replace an Overworld biome entirely or in part.
* Add a sub-biome to an Overworld or modded biome.

* Register custom surface rules.
* Override vanilla surfaces using custom surface builders.

## Examples

A wiki would be nice.  Here's some illegible probably outdated examples instead.

```java
    // replace a vanilla biome 20% of the time
    BiomePlacement.replaceOverworld(BiomeKeys.FOREST, ModBiomeKeys.AUTUMNAL_WOODS, 0.2D);

    // add an edge sub-biome
    BiomePlacement.addSubOverworld(BiomeKeys.FOREST, ModBiomeKeys.OASIS, SubBiomeMatcher.of(SubBiomeMatcher.NEAR_BORDER));

    // add a sub-biome based on noise
    BiomePlacement.addSubOverworld(ModBiomeKeys.LUSH_DESERT, ModBiomeKeys.OASIS, SubBiomeMatcher.of(SubBiomeMatcher.Criterion.ofMax(SubBiomeMatcher.CriterionTargets.PEAKS_VALLEYS, SubBiomeMatcher.CriterionTypes.DISTANCE, 0.2f)));

    // register surface rule(s)
    SurfaceGeneration.addOverworldSurfaceRules(Identifier.of(Mod.MOD_ID, "surface_rules"), modSurfaceRules);

    // use surface builders
    // See the javadocs for this; surface builders are a complicated topic.
    // See also: [Terrestria's surface builders](https://github.com/TerraformersMC/Terrestria/tree/1.19.3/worldgen/src/main/java/com/terraformersmc/terrestria/surfacebuilders)
```