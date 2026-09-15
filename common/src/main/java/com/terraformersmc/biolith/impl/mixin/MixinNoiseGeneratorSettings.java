package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.material.rule.MaterialRule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseGeneratorSettings.class)
public interface MixinNoiseGeneratorSettings {
    @Final
    @Mutable
    @Accessor("materialRule")
    void biolith$setMaterialRule(Holder<MaterialRule> materialRule);
}
