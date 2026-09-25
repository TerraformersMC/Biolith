package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.biolith.api.surface.BiolithSurfaceBuilder;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.surface.SurfaceBuilderCollector;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.ChunkTerrainBuilder;
import net.minecraft.world.level.levelgen.NoiseColumn;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkTerrainBuilder.class)
public abstract class MixinChunkTerrainBuilder {
	@Unique
    private static final Identifier BIOLITH_RANDOM_FACTORY = Identifier.fromNamespaceAndPath(Biolith.MOD_ID, "surface_builders");

	@Shadow
	@Final
	private MaterialRuleContext ruleContext;

	@Shadow
	abstract BlockColumn createBlockColumn(ChunkAccess chunk, int blockX, int blockZ, CarvingMask.@Nullable Column carvingColumn);

	@WrapOperation(method = "fillChunk",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z",
					ordinal = 0
			)
	)
	private boolean biolith$injectSurfaceBuilders(Holder<Biome> instance, ResourceKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) ChunkAccess chunk, @Local NoiseColumn noiseColumn, @Local(name = "blockZ") int blockZ, @Local(name = "blockX") int blockX, @Local(name = "initialHeight") int initialHeight) {
		for (BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
			if (builder.filterBiome(instance)) {
				RandomSource random = ruleContext.getOrCreateRandomFactory(BIOLITH_RANDOM_FACTORY).at(blockX, initialHeight, blockZ);

				builder.generate(ruleContext, noiseColumn, random, chunk, instance.value(), blockX, blockZ, initialHeight);
			}
		}

		return original.call(instance, targetKey);
	}

	@WrapOperation(method = "fillChunk",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/resources/ResourceKey;)Z",
					ordinal = 1
			)
	)
	private boolean biolith$injectLateSurfaceBuilders(Holder<Biome> instance, ResourceKey<Biome> targetKey, Operation<Boolean> original, @Local(argsOnly = true) ChunkAccess chunk, @Local(name = "blockZ") int blockZ, @Local(name = "blockX") int blockX, @Local(name = "initialHeight") int initialHeight, @Local(name = "carvingColumn") CarvingMask.Column carvingColumn) {
		BlockColumn blockColumn = null;

		for (BiolithSurfaceBuilder builder : SurfaceBuilderCollector.getBuilders()) {
			if (builder.filterBiome(instance)) {
				RandomSource random = ruleContext.getOrCreateRandomFactory(BIOLITH_RANDOM_FACTORY).at(blockX, initialHeight, blockZ);
				if (blockColumn == null) {
					blockColumn = this.createBlockColumn(chunk, blockX, blockZ, carvingColumn);
				}

				builder.generateLate(ruleContext, blockColumn, random, chunk, instance.value(), blockX, blockZ, initialHeight);
			}
		}

		return original.call(instance, targetKey);
	}
}
