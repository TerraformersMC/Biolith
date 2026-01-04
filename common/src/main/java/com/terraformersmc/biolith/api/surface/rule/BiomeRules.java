package com.terraformersmc.biolith.api.surface.rule;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.mixin.AccessorMaterialRuleContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

public class BiomeRules {

	public enum Type {
		ACTUAL,
		SURFACE,
		HEIGHTMAP
	}

    public static class BiomeTag implements MaterialRules.MaterialCondition {
        public static final CodecHolder<BiomeTag> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(instance ->
                        instance.group(
                                TagKey.codec(RegistryKeys.BIOME).fieldOf("tag").forGetter(r -> r.biomes)
                        ).apply(instance, BiomeTag::new)
                )
        );

        TagKey<Biome> biomes;

        public BiomeTag(TagKey<Biome> biomes) {
            this.biomes = biomes;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
            return new BiomeTag.Condition(context);
        }

        private final class Condition implements MaterialRules.BooleanSupplier {
            private final AccessorMaterialRuleContext accessor;

            Condition(MaterialRules.MaterialRuleContext context) {
                this.accessor = (AccessorMaterialRuleContext) (Object) context;
            }

            @Override
            public boolean get() {
                return accessor.getBiome().get().isIn(biomes);
            }
        }

        public static MaterialRules.MaterialCondition isBiomeTag(TagKey<Biome> biome) {
            return new BiomeTag(biome);
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
            return CODEC;
        }
    }

	public static class HeightmapBiome implements MaterialRules.MaterialCondition {
		public static final CodecHolder<HeightmapBiome> CODEC = CodecHolder.of(
			RecordCodecBuilder.mapCodec(instance ->
				instance.group(
					RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("biome").forGetter(r -> r.biome)
				).apply(instance, HeightmapBiome::new)
			)
		);

        RegistryKey<Biome> biome;

		public HeightmapBiome(RegistryKey<Biome> biome) {
			this.biome = biome;
		}

		@Override
		public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
			return new HeightmapBiome.MaterialCondition(context);
		}

		private final class MaterialCondition implements MaterialRules.BooleanSupplier {
			private final AccessorMaterialRuleContext accessor;

			private final BlockPos.Mutable currentPos = new BlockPos.Mutable();
			private final BlockPos.Mutable surfacePos = new BlockPos.Mutable();

			private RegistryEntry<Biome> cachedHeightmapBiome = null;
			private int cachedX = Integer.MIN_VALUE;
			private int cachedZ = Integer.MIN_VALUE;

			MaterialCondition(MaterialRules.MaterialRuleContext context) {
				this.accessor = (AccessorMaterialRuleContext) (Object) context;
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
					surfacePos.set(x, surfaceY, z);
					cachedHeightmapBiome = accessor.getBiomeAtPos().apply(surfacePos);
				}

				return cachedHeightmapBiome.matchesKey(biome);
			}
        }

		public static MaterialRules.MaterialCondition isBiome(RegistryKey<Biome> biome) {
			return new HeightmapBiome(biome);
		}

		@Override
		public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
			return CODEC;
		}
	}

    public static class HeightmapBiomeTag implements MaterialRules.MaterialCondition {
        public static final CodecHolder<HeightmapBiomeTag> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(instance ->
                        instance.group(
                                TagKey.codec(RegistryKeys.BIOME).fieldOf("tag").forGetter(r -> r.biomes)
                        ).apply(instance, HeightmapBiomeTag::new)
                )
        );

        TagKey<Biome> biomes;

        public HeightmapBiomeTag(TagKey<Biome> biomes) {
            this.biomes = biomes;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
            return new HeightmapBiomeTag.Condition(context);
        }

        private final class Condition implements MaterialRules.BooleanSupplier {
            private final AccessorMaterialRuleContext accessor;

            private final BlockPos.Mutable currentPos = new BlockPos.Mutable();
            private final BlockPos.Mutable surfacePos = new BlockPos.Mutable();

            private RegistryEntry<Biome> cachedHeightmapBiome = null;
            private int cachedX = Integer.MIN_VALUE;
            private int cachedZ = Integer.MIN_VALUE;

            Condition(MaterialRules.MaterialRuleContext context) {
                this.accessor = (AccessorMaterialRuleContext) (Object) context;
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
                    surfacePos.set(x, surfaceY, z);
                    cachedHeightmapBiome = accessor.getBiomeAtPos().apply(surfacePos);
                }

                return cachedHeightmapBiome.isIn(biomes);
            }
        }

        public static MaterialRules.MaterialCondition isBiomeTag(TagKey<Biome> biome) {
            return new HeightmapBiomeTag(biome);
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
            return CODEC;
        }
    }

    public static class SurfaceBiome implements MaterialRules.MaterialCondition {
        public static final CodecHolder<SurfaceBiome> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(instance ->
                        instance.group(
                                RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("biome").forGetter(r -> r.biome)
                        ).apply(instance, SurfaceBiome::new)
                )
        );

        RegistryKey<Biome> biome;

        public SurfaceBiome(RegistryKey<Biome> biome) {
            this.biome = biome;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
            return new SurfaceBiome.Condition(context);
        }

        private final class Condition implements MaterialRules.BooleanSupplier {
            private final AccessorMaterialRuleContext accessor;
            private final int seaLevel;

            private final BlockPos.Mutable currentPos = new BlockPos.Mutable();
            private final BlockPos.Mutable surfacePos = new BlockPos.Mutable();

            private RegistryEntry<Biome> cachedSurfaceBiome = null;
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
                    cachedSurfaceBiome = accessor.getBiomeAtPos().apply(surfacePos);
                }

                return cachedSurfaceBiome.matchesKey(biome);
            }
        }

        public static MaterialRules.MaterialCondition isBiome(RegistryKey<Biome> biome) {
            return new SurfaceBiome(biome);
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
            return CODEC;
        }
    }

    public static class SurfaceBiomeTag implements MaterialRules.MaterialCondition {
        public static final CodecHolder<SurfaceBiomeTag> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(instance ->
                        instance.group(
                                TagKey.codec(RegistryKeys.BIOME).fieldOf("tag").forGetter(r -> r.biomes)
                        ).apply(instance, SurfaceBiomeTag::new)
                )
        );

        TagKey<Biome> biomes;

        public SurfaceBiomeTag(TagKey<Biome> biomes) {
            this.biomes = biomes;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
            return new SurfaceBiomeTag.Condition(context);
        }

        private final class Condition implements MaterialRules.BooleanSupplier {
            private final AccessorMaterialRuleContext accessor;
            private final int seaLevel;

            private final BlockPos.Mutable currentPos = new BlockPos.Mutable();
            private final BlockPos.Mutable surfacePos = new BlockPos.Mutable();

            private RegistryEntry<Biome> cachedSurfaceBiome = null;
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
                    cachedSurfaceBiome = accessor.getBiomeAtPos().apply(surfacePos);
                }

                return cachedSurfaceBiome.isIn(biomes);
            }
        }

        public static MaterialRules.MaterialCondition isBiomeTag(TagKey<Biome> biome) {
            return new SurfaceBiomeTag(biome);
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
            return CODEC;
        }
    }
}