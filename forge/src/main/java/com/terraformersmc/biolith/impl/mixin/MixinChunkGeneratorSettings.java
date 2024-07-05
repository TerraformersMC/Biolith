package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkGeneratorSettings.class)
public interface MixinChunkGeneratorSettings {
    @Final
    @Mutable
    @Accessor("surfaceRule")
    void biolith$setSurfaceRule(MaterialRules.MaterialRule ruleSource);
}
