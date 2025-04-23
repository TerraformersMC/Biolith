package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.InterfaceBiomeSource;
import com.terraformersmc.biolith.impl.compat.VanillaCompat;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import terrablender.core.TerraBlender;

/*
 * This entire mixin is basically a work-around for TerraBlender's HEAD mixin and @Unique variables.
 * If TerraBlender gets a proper API for End generation, maybe we can toss this in favor of something better.
 * Unlike most Biolith overlays, this one may select our biomes with preference over TerraBlender's biomes.
 *
 * This mixin is duplicated for each loader because the platform services are not available at common mixin
 * load and the refmaps for the platforms do not include the refs of mixins defined in the common project.
 * TODO: Review this when upgrading to arch loom 1.5.
 *
 * Priority 900 places this mixin in front of both TerraBlender and our main TheEndBiomeSource mixin.
 */
@Mixin(value = TheEndBiomeSource.class, priority = 900)
public abstract class MixinTBTheEndBiomeSource extends BiomeSource {
    @Unique
    private static final ThreadLocal<Boolean> bypass = ThreadLocal.withInitial(() -> false);

    public boolean biolith$getBypass() {
        return bypass.get();
    }

    public void biolith$setBypass(boolean value) {
        bypass.set(value);
    }

    @Inject(method = "getBiome", at = @At("HEAD"), cancellable = true)
    private void biolith$getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Allows us to call unmodified (by us) getBiome() to get TerraBlender values.
        if (bypass.get()) {
            return;
        }

        // Fetch whatever TerraBlender thinks the biome should be, which we will call the original biome.
        bypass.set(true);
        RegistryEntry<Biome> original = this.getBiome(x, y, z, noise);
        bypass.set(false);

        // Fake up a noise point for sub biome placement.
        MultiNoiseUtil.NoiseValuePoint noisePoint = BiomeCoordinator.END.sampleEndNoise(x, y, z, noise, original);

        // Select noise biome
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes = VanillaCompat.getEndBiome(noisePoint, ((InterfaceBiomeSource)this).biolith$getBiomeEntries(), original);

        // Process any replacements or sub-biomes.
        cir.setReturnValue(BiomeCoordinator.END.getReplacement(x, y, z, noisePoint, fittestNodes));
    }

    /*
     * Under normal conditions, TerraBlender will have already canceled, but if bclib is present, it may not.
     * This ensures our main getBiome() mixin will not be called when bypass is true.
     */
    @Inject(method = "getBiome", at = @At("RETURN"), cancellable = true)
    private void biolith$cancelGetBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise, CallbackInfoReturnable<RegistryEntry<Biome>> cir) {
        // Allows us to call unmodified (by us) getBiome() to get TerraBlender values.
        if (bypass.get()) {
            cir.setReturnValue(cir.getReturnValue());
        }
    }
}