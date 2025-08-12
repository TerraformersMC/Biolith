package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.biolith.api.surface.BiolithSurfaceBuilder;
import com.terraformersmc.biolith.impl.surface.SurfaceBuilderCollector;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurfaceBuilder.class)
public class MixinSurfaceBuilder {
	@Shadow
	@Final
	private RandomSplitter randomDeriver;

	@Shadow
	@Final
	private int seaLevel;

	@WrapOperation(method = "buildSurface",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/registry/entry/RegistryEntry;matchesKey(Lnet/minecraft/registry/RegistryKey;)Z",
					ordinal = 0
			)
	)
	private boolean biolith$injectSurfaceBuilders(RegistryEntry<Biome> instance, RegistryKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) BiomeAccess biomeAccess, @Local(argsOnly = true) Chunk chunk, @Local BlockColumn blockColumn, @Local(ordinal = 4) int m, @Local(ordinal = 5) int n, @Local(ordinal = 6) int o) {
		Random random = randomDeriver.split(m, o, n);

		for (BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
			if (builder.filterBiome(instance)) {
				builder.generate(biomeAccess, blockColumn, random, chunk, instance.value(), m, n, o, seaLevel);
			}
		}

		return original.call(instance, targetKey);
	}

	@WrapOperation(method = "buildSurface",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/registry/entry/RegistryEntry;matchesKey(Lnet/minecraft/registry/RegistryKey;)Z",
					ordinal = 1
			)
	)
	private boolean biolith$injectLateSurfaceBuilders(RegistryEntry<Biome> instance, RegistryKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) BiomeAccess biomeAccess, @Local(argsOnly = true) Chunk chunk, @Local BlockColumn blockColumn, @Local MaterialRules.MaterialRuleContext materialRuleContext, @Local(ordinal = 4) int m, @Local(ordinal = 5) int n, @Local(ordinal = 6) int o) {
		Random random = randomDeriver.split(m, o, n);
		int surfaceMinY = materialRuleContext.estimateSurfaceHeight();

		for (BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
			if (builder.filterBiome(instance)) {
				builder.generateLate(biomeAccess, blockColumn, random, chunk, instance.value(), m, n, o, seaLevel, surfaceMinY);
			}
		}

		return original.call(instance, targetKey);
	}
}
