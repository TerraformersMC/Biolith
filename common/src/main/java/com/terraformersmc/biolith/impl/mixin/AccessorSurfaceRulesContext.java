package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(SurfaceRules.Context.class)
public interface AccessorSurfaceRulesContext {
	@Accessor("biome")
	Supplier<Holder<Biome>> getBiome();
	@Accessor("biomeGetter")
	Function<BlockPos, Holder<Biome>> getBiomeAtPos();
	@Accessor("chunk")
    ChunkAccess getChunk();
	@Accessor("pos")
	BlockPos.MutableBlockPos getBlockPos();
	@Accessor("blockX")
	int getBlockX();
	@Accessor("blockY")
	int getBlockY();
	@Accessor("blockZ")
	int getBlockZ();
	@Accessor("system")
    SurfaceSystem getSystem();
	@Accessor("randomState")
    RandomState getRandomState();
}