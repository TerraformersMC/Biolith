package com.terraformersmc.biolith.api.surface.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.mixin.AccessorBiome;
import com.terraformersmc.biolith.impl.mixin.AccessorMaterialRuleContext;
import com.terraformersmc.biolith.impl.tag.CommonBiomeTags;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

public class ClimateRules {
	public static class Temperature implements MaterialRules.MaterialCondition {
		public static final CodecHolder<Temperature> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;
			private final int seaLevel;

			private final BlockPos.Mutable currentPos = new BlockPos.Mutable();
			private final BlockPos.Mutable surfacePos = new BlockPos.Mutable();

			private RegistryEntry<Biome> cachedBiome = null;
			private boolean isCachedMountain = false;
			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = accessor.getBlockY();
				int z = accessor.getBlockZ();
				currentPos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					int surfaceY = accessor.getChunk().sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, x, z);
					surfacePos.set(x, Math.max(surfaceY, seaLevel), z);
					cachedBiome = accessor.getBiomeAtPos().apply(surfacePos);

					isCachedMountain = cachedBiome.isIn(CommonBiomeTags.IS_MOUNTAIN);
				}

				RegistryEntry<Biome> biomeToUse;

				if (isCachedMountain) {
					biomeToUse = accessor.getBiomeAtPos().apply(currentPos);
					if (biomeToUse.isIn(CommonBiomeTags.IS_CAVE) && y >= 0) biomeToUse = cachedBiome;
				} else {
					biomeToUse = cachedBiome;
				}

				Biome biome = biomeToUse.value();
				float adjustedTemp = biome.getTemperature(currentPos, seaLevel);

				return adjustedTemp >= ClimateRules.Temperature.this.min && adjustedTemp <= ClimateRules.Temperature.this.max;
			}
		}

		public static MaterialRules.MaterialCondition point(float point) {
			return new ClimateRules.Temperature(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new ClimateRules.Temperature(min, max);
		}

		public static MaterialRules.MaterialCondition above(float point) {
			return new ClimateRules.Temperature(point, Float.MAX_VALUE);
		}

		public static MaterialRules.MaterialCondition below(float point) {
			return new ClimateRules.Temperature(Float.MIN_VALUE, point);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

	public static class TemperatureOffset implements MaterialRules.MaterialCondition {
		public static final CodecHolder<TemperatureOffset> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;
			private final int seaLevel;

			private final BlockPos.Mutable currentPos = new BlockPos.Mutable();
			private final BlockPos.Mutable surfacePos = new BlockPos.Mutable();

			private RegistryEntry<Biome> cachedBiome = null;
			private boolean isCachedMountain = false;
			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = accessor.getBlockY();
				int z = accessor.getBlockZ();
				currentPos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					int surfaceY = accessor.getChunk().sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, x, z);
					surfacePos.set(x, Math.max(surfaceY, seaLevel), z);
					cachedBiome = accessor.getBiomeAtPos().apply(surfacePos);

					isCachedMountain = cachedBiome.isIn(CommonBiomeTags.IS_MOUNTAIN);
				}

				RegistryEntry<Biome> biomeToUse;

				if (isCachedMountain) {
					biomeToUse = accessor.getBiomeAtPos().apply(currentPos);
					if (biomeToUse.isIn(CommonBiomeTags.IS_CAVE) && y >= 0) biomeToUse = cachedBiome;
				} else {
					biomeToUse = cachedBiome;
				}

				Biome biome = biomeToUse.value();
				float tempOffset = biome.getTemperature(currentPos, seaLevel) - biome.getTemperature();

				return tempOffset >= ClimateRules.TemperatureOffset.this.min && tempOffset <= ClimateRules.TemperatureOffset.this.max;
			}
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new ClimateRules.TemperatureOffset(min, max);
		}

		public static MaterialRules.MaterialCondition above(float point) {
			return new ClimateRules.TemperatureOffset(point, Float.MAX_VALUE);
		}

		public static MaterialRules.MaterialCondition below(float point) {
			return new ClimateRules.TemperatureOffset(Float.MIN_VALUE, point);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

	public static class Downfall implements MaterialRules.MaterialCondition {

		public static final CodecHolder<Downfall> CODEC = CodecHolder.of(
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
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new Downfall.Condition(context);
		}

		private final class Condition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;
			private final int seaLevel;

			private final BlockPos.Mutable currentPos = new BlockPos.Mutable();
			private final BlockPos.Mutable surfacePos = new BlockPos.Mutable();

			private RegistryEntry<Biome> cachedBiome = null;
			private boolean isCachedMountain = false;
			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			Condition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
				this.seaLevel = accessor.getSystem().getSeaLevel();
			}

			@Override
			public boolean get() {
				int x = accessor.getBlockX();
				int y = accessor.getBlockY();
				int z = accessor.getBlockZ();
				currentPos.set(x, y, z);

				// Handle caching
				if (x != cachedX || z != cachedZ) {
					cachedX = x;
					cachedZ = z;

					int surfaceY = accessor.getChunk().sampleHeightmap(Heightmap.Type.OCEAN_FLOOR_WG, x, z);
					surfacePos.set(x, Math.max(surfaceY, seaLevel), z);
					cachedBiome = accessor.getBiomeAtPos().apply(surfacePos);

					isCachedMountain = cachedBiome.isIn(CommonBiomeTags.IS_MOUNTAIN);
				}

				RegistryEntry<Biome> biomeToUse;

				if (isCachedMountain) {
					biomeToUse = accessor.getBiomeAtPos().apply(currentPos);
					if (biomeToUse.isIn(CommonBiomeTags.IS_CAVE) && y >= 0) biomeToUse = cachedBiome;
				} else {
					biomeToUse = cachedBiome;
				}

				Biome biome = biomeToUse.value();
				Biome.Weather climate = ((AccessorBiome) (Object) biome).getClimate();
				float downfall = climate.downfall();

				return downfall >= ClimateRules.Downfall.this.min && downfall <= ClimateRules.Downfall.this.max;
			}
		}

		public static MaterialRules.MaterialCondition point(float point) {
			return new ClimateRules.Downfall(point - 0.001F, point + 0.001F);
		}

		public static MaterialRules.MaterialCondition range(float min, float max) {
			return new ClimateRules.Downfall(min, max);
		}

		public static MaterialRules.MaterialCondition above(float point) {
			return new ClimateRules.Downfall(point, Float.MAX_VALUE);
		}

		public static MaterialRules.MaterialCondition below(float point) {
			return new ClimateRules.Downfall(Float.MIN_VALUE, point);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}
}