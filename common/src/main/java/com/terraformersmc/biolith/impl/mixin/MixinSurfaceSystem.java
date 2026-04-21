package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.biolith.api.surface.BiolithSurfaceBuilder;
import com.terraformersmc.biolith.impl.surface.SurfaceBuilderCollector;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurfaceSystem.class)
public class MixinSurfaceSystem {
	@Shadow
	@Final
	private PositionalRandomFactory noiseRandom;

	@Shadow
	@Final
	private int seaLevel;

	@WrapOperation(method = "buildSurface",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z",
					ordinal = 0
			)
	)
	private boolean biolith$injectSurfaceBuilders(Holder<Biome> instance, ResourceKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) BiomeManager biomeAccess, @Local(argsOnly = true) ChunkAccess chunk, @Local BlockColumn blockColumn, @Local(ordinal = 4) int m, @Local(ordinal = 5) int n, @Local(ordinal = 6) int o) {
		RandomSource random = noiseRandom.at(m, o, n);

		for (BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
			if (builder.filterBiome(instance)) {
				builder.generate(biomeAccess, blockColumn, random, chunk, instance.value(), m, n, o, seaLevel);
			}
		}

		return original.call(instance, targetKey);
	}

	@WrapOperation(method = "buildSurface",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z",
					ordinal = 1
			)
	)
	private boolean biolith$injectLateSurfaceBuilders(Holder<Biome> instance, ResourceKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) BiomeManager biomeAccess, @Local(argsOnly = true) ChunkAccess chunk, @Local BlockColumn blockColumn, @Local SurfaceRules.Context materialRuleContext, @Local(ordinal = 4) int m, @Local(ordinal = 5) int n, @Local(ordinal = 6) int o) {
		RandomSource random = noiseRandom.at(m, o, n);
		int surfaceMinY = materialRuleContext.getMinSurfaceLevel();

		for (BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
			if (builder.filterBiome(instance)) {
				builder.generateLate(biomeAccess, blockColumn, random, chunk, instance.value(), m, n, o, seaLevel, surfaceMinY);
			}
		}

		return original.call(instance, targetKey);
	}
}
