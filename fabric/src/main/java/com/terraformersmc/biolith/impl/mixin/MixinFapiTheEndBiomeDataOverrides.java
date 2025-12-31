package com.terraformersmc.biolith.impl.mixin;

import net.fabricmc.fabric.impl.biome.TheEndBiomeData;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TheEndBiomeData.Overrides.class)
public class MixinFapiTheEndBiomeDataOverrides {
    /*
     * Not only is FAPI not responsible for End biome placement under Biolith, but also they arbitrarily decided
     * to block mods from adding End biomes via alternative means.  We bypass their picker to avoid this check.
     */
    @Inject(method = "pick(IIILnet/minecraft/world/level/biome/Climate$Sampler;Lnet/minecraft/core/Holder;)Lnet/minecraft/core/Holder;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void biolith$bypassBiomeAPI(int x, int y, int z, Climate.Sampler noise, Holder<Biome> vanillaBiome, CallbackInfoReturnable<Holder<Biome>> cir) {
        cir.setReturnValue(vanillaBiome);
    }
}
