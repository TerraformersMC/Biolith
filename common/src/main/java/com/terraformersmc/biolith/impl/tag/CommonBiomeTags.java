package com.terraformersmc.biolith.impl.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class CommonBiomeTags {

    // tags are used to allow better-looking faking of accurate climate rules w/o re-calculating the biome for each block of matching x/z position in order to preserve performance
    // these use common tags and are thus shared by Fabric & NeoForge - they've been added here manually due to the multiloader environment
    public static final TagKey<Biome> IS_MOUNTAIN = register("is_mountain");
    public static final TagKey<Biome> IS_CAVE = register("is_cave");

    private static TagKey<Biome> register(String path) {
        return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", path));
    }
}
