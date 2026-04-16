package com.terraformersmc.biolith.impl.surface.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.mixin.AccessorSurfaceRulesContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class NoiseRules {

	public enum Type {
		HUMIDITY,
		CONTINENTALNESS,
		TEMPERATURE,
		EROSION,
		WEIRDNESS,
		DEPTH,
		HEIGHTMAP_DEPTH
	}

	public static class Temperature implements SurfaceRules.ConditionSource {

		public static final KeyDispatchDataCodec<Temperature> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, NoiseRules.Temperature::new)
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

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().router().temperature().compute(new DensityFunction.FunctionContext() {
						@Override
						public int blockX() {
							return x;
						}

						@Override
						public int blockY() {
							return y;
						}

						@Override
						public int blockZ() {
							return z;
						}
					});
				}

				return cachedNoise >= NoiseRules.Temperature.this.min && cachedNoise <= NoiseRules.Temperature.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new NoiseRules.Temperature(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new NoiseRules.Temperature(min, max);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}

	public static class Humidity implements SurfaceRules.ConditionSource {

		public static final KeyDispatchDataCodec<NoiseRules.Humidity> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, NoiseRules.Humidity::new)
			)
		);

		private final float min;
		private final float max;

		public Humidity(float min, float max) {
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

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().router().vegetation().compute(new DensityFunction.FunctionContext() {
						@Override
						public int blockX() {
							return x;
						}

						@Override
						public int blockY() {
							return y;
						}

						@Override
						public int blockZ() {
							return z;
						}
					});
				}

				return cachedNoise >= NoiseRules.Humidity.this.min && cachedNoise <= NoiseRules.Humidity.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new NoiseRules.Humidity(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new NoiseRules.Humidity(min, max);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}

	public static class Erosion implements SurfaceRules.ConditionSource {

		public static final KeyDispatchDataCodec<NoiseRules.Erosion> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, NoiseRules.Erosion::new)
			)
		);

		private final float min;
		private final float max;

		public Erosion(float min, float max) {
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

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().router().erosion().compute(new DensityFunction.FunctionContext() {
						@Override
						public int blockX() {
							return x;
						}

						@Override
						public int blockY() {
							return y;
						}

						@Override
						public int blockZ() {
							return z;
						}
					});
				}

				return cachedNoise >= NoiseRules.Erosion.this.min && cachedNoise <= NoiseRules.Erosion.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new NoiseRules.Erosion(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new NoiseRules.Erosion(min, max);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}

	public static class Continentalness implements SurfaceRules.ConditionSource {

		public static final KeyDispatchDataCodec<NoiseRules.Continentalness> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, NoiseRules.Continentalness::new)
			)
		);

		private final float min;
		private final float max;

		public Continentalness(float min, float max) {
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

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().router().continents().compute(new DensityFunction.FunctionContext() {
						@Override
						public int blockX() {
							return x;
						}

						@Override
						public int blockY() {
							return y;
						}

						@Override
						public int blockZ() {
							return z;
						}
					});
				}

				return cachedNoise >= NoiseRules.Continentalness.this.min && cachedNoise <= NoiseRules.Continentalness.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new NoiseRules.Continentalness(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new NoiseRules.Continentalness(min, max);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}

	public static class Weirdness implements SurfaceRules.ConditionSource {

		public static final KeyDispatchDataCodec<NoiseRules.Weirdness> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, NoiseRules.Weirdness::new)
			)
		);

		private final float min;
		private final float max;

		public Weirdness(float min, float max) {
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

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().router().ridges().compute(new DensityFunction.FunctionContext() {
						@Override
						public int blockX() {
							return x;
						}

						@Override
						public int blockY() {
							return y;
						}

						@Override
						public int blockZ() {
							return z;
						}
					});
				}

				return cachedNoise >= NoiseRules.Weirdness.this.min && cachedNoise <= NoiseRules.Weirdness.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new NoiseRules.Weirdness(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new NoiseRules.Weirdness(min, max);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}

	public static class Depth implements SurfaceRules.ConditionSource {

		public static final KeyDispatchDataCodec<NoiseRules.Depth> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, NoiseRules.Depth::new)
			)
		);

		private final float min;
		private final float max;

		public Depth(float min, float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public SurfaceRules.Condition  apply(SurfaceRules.Context context) {
			return new Condition(context);
		}

		private final class Condition implements SurfaceRules.Condition  {
			private final AccessorSurfaceRulesContext accessor;

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int y = accessor.getBlockY();
				int z = accessor.getBlockZ();

				double noise = accessor.getRandomState().router().depth().compute(new DensityFunction.FunctionContext() {
					@Override
					public int blockX() {
						return x;
					}

					@Override
					public int blockY() {
						return y;
					}

					@Override
					public int blockZ() {
						return z;
					}
				});

				return noise >= NoiseRules.Depth.this.min && noise <= NoiseRules.Depth.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new NoiseRules.Depth(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new NoiseRules.Depth(min, max);
		}

		public static SurfaceRules.ConditionSource above(float above) {
			return new NoiseRules.Depth(above, Float.MAX_VALUE);
		}

		public static SurfaceRules.ConditionSource below(float below) {
			return new NoiseRules.Depth(Float.MIN_VALUE, below);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}

	public static class HeightmapDepth implements SurfaceRules.ConditionSource {

		public static final KeyDispatchDataCodec<NoiseRules.HeightmapDepth> CODEC = KeyDispatchDataCodec.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					Codec.FLOAT.fieldOf("min").forGetter(r -> r.min),
					Codec.FLOAT.fieldOf("max").forGetter(r -> r.max)
				).apply(instance, NoiseRules.HeightmapDepth::new)
			)
		);

		private final float min;
		private final float max;

		public HeightmapDepth(float min, float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public SurfaceRules.Condition  apply(SurfaceRules.Context context) {
			return new Condition(context);
		}

		private final class Condition implements SurfaceRules.Condition  {
			private final AccessorSurfaceRulesContext accessor;

			private double cachedNoise;

			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			Condition(SurfaceRules.Context context) {
				this.accessor = (AccessorSurfaceRulesContext) (Object) context;
			}

			@Override
			public boolean test() {
				int x = accessor.getBlockX();
				int z = accessor.getBlockZ();

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					cachedNoise = accessor.getRandomState().router().depth().compute(new DensityFunction.FunctionContext() {
						@Override
						public int blockX() {
							return x;
						}

						@Override
						public int blockY() {
							return accessor.getChunk().getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
						}

						@Override
						public int blockZ() {
							return z;
						}
					});
				}

				return cachedNoise >= NoiseRules.HeightmapDepth.this.min && cachedNoise <= NoiseRules.HeightmapDepth.this.max;
			}
		}

		public static SurfaceRules.ConditionSource point(float point) {
			return new NoiseRules.Depth(point - 0.001F, point + 0.001F);
		}

		public static SurfaceRules.ConditionSource range(float min, float max) {
			return new NoiseRules.Depth(min, max);
		}

		public static SurfaceRules.ConditionSource above(float above) {
			return new NoiseRules.Depth(above, Float.MAX_VALUE);
		}

		public static SurfaceRules.ConditionSource below(float below) {
			return new NoiseRules.Depth(Float.MIN_VALUE, below);
		}

		@Override
		public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
			return CODEC;
		}
	}
}