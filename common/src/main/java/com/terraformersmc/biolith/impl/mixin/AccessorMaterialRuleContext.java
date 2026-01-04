package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(MaterialRules.MaterialRuleContext.class)
public interface AccessorMaterialRuleContext {
	@Accessor("biomeSupplier")
    Supplier<RegistryEntry<Biome>> getBiome();
	@Accessor("posToBiome")
    Function<BlockPos, RegistryEntry<Biome>> getBiomeAtPos();
	@Accessor("chunk")
    Chunk getChunk();
	@Accessor("pos")
	BlockPos.Mutable getBlockPos();
	@Accessor("blockX")
	int getBlockX();
	@Accessor("blockY")
	int getBlockY();
	@Accessor("blockZ")
	int getBlockZ();
	@Accessor("surfaceBuilder")
    SurfaceBuilder getSystem();
	@Accessor("noiseConfig")
    NoiseConfig getRandomState();
}