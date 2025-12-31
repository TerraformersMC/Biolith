package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseGeneratorSettings.class)
public interface MixinChunkGeneratorSettings {
    @Final
    @Mutable
    @Accessor("surfaceRule")
    void biolith$setSurfaceRule(SurfaceRules.RuleSource ruleSource);
}
