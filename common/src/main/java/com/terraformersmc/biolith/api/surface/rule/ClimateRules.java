package com.terraformersmc.biolith.api.surface.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.mixin.AccessorBiome;
import com.terraformersmc.biolith.impl.mixin.AccessorSurfaceRulesContext;
import com.terraformersmc.biolith.impl.tag.CommonBiomeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class ClimateRules {
	public static class Temperature implements SurfaceRules.ConditionSource {
		public static final KeyDispatchDataCodec<Temperature> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, ClimateRules.Temperature::new)
			)
		);

		private final float min;
		private final float max;

		public Temperature(float min, float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public SurfaceRules.Condition  apply(SurfaceRules.Context context) {
			return new Condition(context);
		}

		private final class Condition implements SurfaceRules.Condition  {
			private final AccessorSurfaceRulesContext accessor;
			private final int seaLevel;

			private final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
			private final BlockPos.MutableBlockPos surfacePos = new BlockPos.MutableBlockPos();

			private Holder<Biome> cachedBiome = null;
			private boolean isCachedMountain = false;
			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = accessor.getBlockY();
				int z = accessor.getBlockZ();
				currentPos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					int surfaceY = accessor.getChunk().getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
					surfacePos.set(x, Math.max(surfaceY, seaLevel), z);
					cachedBiome = accessor.getBiomeAtPos().apply(surfacePos);

					isCachedMountain = cachedBiome.is(CommonBiomeTags.IS_MOUNTAIN);
				}

				Holder<Biome> biomeToUse;

				if (isCachedMountain) {
					biomeToUse = accessor.getBiomeAtPos().apply(currentPos);
					if (biomeToUse.is(CommonBiomeTags.IS_CAVE) && y >= 0) biomeToUse = cachedBiome;
				} else {
					biomeToUse = cachedBiome;
				}

				Biome biome = biomeToUse.value();
				float adjustedTemp = biome.getHeightAdjustedTemperature(currentPos, seaLevel);

				return adjustedTemp >= ClimateRules.Temperature.this.min && adjustedTemp <= ClimateRules.Temperature.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new ClimateRules.Temperature(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new ClimateRules.Temperature(min, max);
		}

		public static SurfaceRules.ConditionSource above(float point) {
			return new ClimateRules.Temperature(point, Float.MAX_VALUE);
		}

		public static SurfaceRules.ConditionSource below(float point) {
			return new ClimateRules.Temperature(Float.MIN_VALUE, point);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}

	public static class TemperatureOffset implements SurfaceRules.ConditionSource {
		public static final KeyDispatchDataCodec<TemperatureOffset> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, ClimateRules.TemperatureOffset::new)
			)
		);

		private final float min;
		private final float max;

		public TemperatureOffset(float min, float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public SurfaceRules.Condition  apply(SurfaceRules.Context context) {
			return new Condition(context);
		}

		private final class Condition implements SurfaceRules.Condition  {
			private final AccessorSurfaceRulesContext accessor;
			private final int seaLevel;

			private final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
			private final BlockPos.MutableBlockPos surfacePos = new BlockPos.MutableBlockPos();

			private Holder<Biome> cachedBiome = null;
			private boolean isCachedMountain = false;
			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = accessor.getBlockY();
				int z = accessor.getBlockZ();
				currentPos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					int surfaceY = accessor.getChunk().getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
					surfacePos.set(x, Math.max(surfaceY, seaLevel), z);
					cachedBiome = accessor.getBiomeAtPos().apply(surfacePos);

					isCachedMountain = cachedBiome.is(CommonBiomeTags.IS_MOUNTAIN);
				}

				Holder<Biome> biomeToUse;

				if (isCachedMountain) {
					biomeToUse = accessor.getBiomeAtPos().apply(currentPos);
					if (biomeToUse.is(CommonBiomeTags.IS_CAVE) && y >= 0) biomeToUse = cachedBiome;
				} else {
					biomeToUse = cachedBiome;
				}

				Biome biome = biomeToUse.value();
				float tempOffset = biome.getHeightAdjustedTemperature(currentPos, seaLevel) - biome.getBaseTemperature();

				return tempOffset >= ClimateRules.TemperatureOffset.this.min && tempOffset <= ClimateRules.TemperatureOffset.this.max;
			}
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new ClimateRules.TemperatureOffset(min, max);
		}

		public static SurfaceRules.ConditionSource above(float point) {
			return new ClimateRules.TemperatureOffset(point, Float.MAX_VALUE);
		}

		public static SurfaceRules.ConditionSource below(float point) {
			return new ClimateRules.TemperatureOffset(Float.MIN_VALUE, point);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}

	public static class Downfall implements SurfaceRules.ConditionSource {

		public static final KeyDispatchDataCodec<Downfall> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, ClimateRules.Downfall::new)
			)
		);

		private final float min;
		private final float max;

		public Downfall(float min, float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public SurfaceRules.Condition  apply(SurfaceRules.Context context) {
			return new Downfall.Condition(context);
		}

		private final class Condition implements SurfaceRules.Condition  {
			private final AccessorSurfaceRulesContext accessor;
			private final int seaLevel;

			private final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
			private final BlockPos.MutableBlockPos surfacePos = new BlockPos.MutableBlockPos();

			private Holder<Biome> cachedBiome = null;
			private boolean isCachedMountain = false;
			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = accessor.getBlockY();
				int z = accessor.getBlockZ();
				currentPos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					int surfaceY = accessor.getChunk().getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
					surfacePos.set(x, Math.max(surfaceY, seaLevel), z);
					cachedBiome = accessor.getBiomeAtPos().apply(surfacePos);

					isCachedMountain = cachedBiome.is(CommonBiomeTags.IS_MOUNTAIN);
				}

				Holder<Biome> biomeToUse;

				if (isCachedMountain) {
					biomeToUse = accessor.getBiomeAtPos().apply(currentPos);
					if (biomeToUse.is(CommonBiomeTags.IS_CAVE) && y >= 0) biomeToUse = cachedBiome;
				} else {
					biomeToUse = cachedBiome;
				}

				Biome biome = biomeToUse.value();
				Biome.ClimateSettings climate = ((AccessorBiome) (Object) biome).getClimate();
				float downfall = climate.downfall();

				return downfall >= ClimateRules.Downfall.this.min && downfall <= ClimateRules.Downfall.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new ClimateRules.Downfall(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new ClimateRules.Downfall(min, max);
		}

		public static SurfaceRules.ConditionSource above(float point) {
			return new ClimateRules.Downfall(point, Float.MAX_VALUE);
		}

		public static SurfaceRules.ConditionSource below(float point) {
			return new ClimateRules.Downfall(Float.MIN_VALUE, point);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}
}