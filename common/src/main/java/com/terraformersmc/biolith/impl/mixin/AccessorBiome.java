package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Biome.class)
public interface AccessorBiome {
    @Accessor("climateSettings")
    Biome.ClimateSettings getClimate();
}