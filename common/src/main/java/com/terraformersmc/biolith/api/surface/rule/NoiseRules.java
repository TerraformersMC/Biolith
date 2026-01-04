package com.terraformersmc.biolith.api.surface.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.mixin.AccessorMaterialRuleContext;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

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

	public static class Temperature implements MaterialRules.MaterialCondition {

		public static final CodecHolder<Temperature> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;
			private final int seaLevel;

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.Mutable pos = new BlockPos.Mutable();

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().getNoiseRouter().temperature().sample(new DensityFunction.NoisePos() {
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

		public static MaterialRules.MaterialCondition point(float point) {
			return new NoiseRules.Temperature(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new NoiseRules.Temperature(min, max);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

	public static class Humidity implements MaterialRules.MaterialCondition {

		public static final CodecHolder<NoiseRules.Humidity> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;
			private final int seaLevel;

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.Mutable pos = new BlockPos.Mutable();

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().getNoiseRouter().vegetation().sample(new DensityFunction.NoisePos() {
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

		public static MaterialRules.MaterialCondition point(float point) {
			return new NoiseRules.Humidity(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new NoiseRules.Humidity(min, max);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

	public static class Erosion implements MaterialRules.MaterialCondition {

		public static final CodecHolder<NoiseRules.Erosion> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;
			private final int seaLevel;

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.Mutable pos = new BlockPos.Mutable();

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().getNoiseRouter().erosion().sample(new DensityFunction.NoisePos() {
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

		public static MaterialRules.MaterialCondition point(float point) {
			return new NoiseRules.Erosion(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new NoiseRules.Erosion(min, max);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

	public static class Continentalness implements MaterialRules.MaterialCondition {

		public static final CodecHolder<NoiseRules.Continentalness> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;
			private final int seaLevel;

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.Mutable pos = new BlockPos.Mutable();

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().getNoiseRouter().continents().sample(new DensityFunction.NoisePos() {
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

		public static MaterialRules.MaterialCondition point(float point) {
			return new NoiseRules.Continentalness(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new NoiseRules.Continentalness(min, max);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

	public static class Weirdness implements MaterialRules.MaterialCondition {

		public static final CodecHolder<NoiseRules.Weirdness> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;
			private final int seaLevel;

			private double cachedNoise;
			private int cachedX;
			private int cachedZ;

			private final BlockPos.Mutable pos = new BlockPos.Mutable();

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = this.seaLevel;
				int z = accessor.getBlockZ();
				pos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					this.cachedNoise = accessor.getRandomState().getNoiseRouter().ridges().sample(new DensityFunction.NoisePos() {
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

		public static MaterialRules.MaterialCondition point(float point) {
			return new NoiseRules.Weirdness(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new NoiseRules.Weirdness(min, max);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

	public static class Depth implements MaterialRules.MaterialCondition {

		public static final CodecHolder<NoiseRules.Depth> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = accessor.getBlockY();
				int z = accessor.getBlockZ();

				double noise = accessor.getRandomState().getNoiseRouter().depth().sample(new DensityFunction.NoisePos() {
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

		public static MaterialRules.MaterialCondition point(float point) {
			return new NoiseRules.Depth(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new NoiseRules.Depth(min, max);
		}

		public static MaterialRules.MaterialCondition above(float above) {
			return new NoiseRules.Depth(above, Float.MAX_VALUE);
		}

		public static MaterialRules.MaterialCondition below(float below) {
			return new NoiseRules.Depth(Float.MIN_VALUE, below);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

	public static class HeightmapDepth implements MaterialRules.MaterialCondition {

		public static final CodecHolder<NoiseRules.HeightmapDepth> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;

			private double cachedNoise;

			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int z = accessor.getBlockZ();

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					cachedNoise = accessor.getRandomState().getNoiseRouter().depth().sample(new DensityFunction.NoisePos() {
						@Override
						public int blockX() {
							return x;
						}

						@Override
						public int blockY() {
							return accessor.getChunk().sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, x, z);
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

		public static MaterialRules.MaterialCondition point(float point) {
			return new NoiseRules.Depth(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new NoiseRules.Depth(min, max);
		}

		public static MaterialRules.MaterialCondition above(float above) {
			return new NoiseRules.Depth(above, Float.MAX_VALUE);
		}

		public static MaterialRules.MaterialCondition below(float below) {
			return new NoiseRules.Depth(Float.MIN_VALUE, below);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}
}