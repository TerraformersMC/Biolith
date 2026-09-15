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
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.MaterialSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MaterialSystem.class)
public class MixinMaterialSystem {
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
	private boolean biolith$injectSurfaceBuilders(Holder<Biome> instance, ResourceKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) BiomeManager biomeAccess, @Local(argsOnly = true) ChunkAccess chunk, @Local BlockColumn blockColumn, @Local(name = "blockX") int blockX, @Local(name = "blockZ") int blockZ, @Local(name = "startingHeight") int startingHeight) {
		RandomSource random = noiseRandom.at(blockX, startingHeight, blockZ);

		for (BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
			if (builder.filterBiome(instance)) {
				builder.generate(biomeAccess, blockColumn, random, chunk, instance.value(), blockX, blockZ, startingHeight, seaLevel);
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
	private boolean biolith$injectLateSurfaceBuilders(Holder<Biome> instance, ResourceKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) BiomeManager biomeAccess, @Local(argsOnly = true) ChunkAccess chunk, @Local BlockColumn blockColumn, @Local MaterialRuleContext materialRuleContext, @Local(name = "blockX") int blockX, @Local(name = "blockZ") int blockZ, @Local(name = "startingHeight") int startingHeight) {
		RandomSource random = noiseRandom.at(blockX, startingHeight, blockZ);
		int surfaceMinY = materialRuleContext.getMinSurfaceLevel();

		for (BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
			if (builder.filterBiome(instance)) {
				builder.generateLate(biomeAccess, blockColumn, random, chunk, instance.value(), blockX, blockZ, startingHeight, seaLevel, surfaceMinY);
			}
		}

		return original.call(instance, targetKey);
	}
}
