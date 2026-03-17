package com.terraformersmc.biolith.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.List;

public record BiomePlacementMarshaller(List<AddBiomeMarshaller> additions, List<RemoveBiomeMarshaller> removals, List<ReplaceBiomeMarshaller> replacements, List<AddSubBiomeMarshaller> subBiomes) {
    public static final Codec<BiomePlacementMarshaller> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                            AddBiomeMarshaller.CODEC.listOf().optionalFieldOf("additions", List.of())
                                    .forGetter(BiomePlacementMarshaller::additions),
                            RemoveBiomeMarshaller.CODEC.listOf().optionalFieldOf("removals", List.of())
                                    .forGetter(BiomePlacementMarshaller::removals),
                            ReplaceBiomeMarshaller.CODEC.listOf().optionalFieldOf("replacements", List.of())
                                    .forGetter(BiomePlacementMarshaller::replacements),
                            AddSubBiomeMarshaller.CODEC.listOf().optionalFieldOf("sub_biomes", List.of())
                                    .forGetter(BiomePlacementMarshaller::subBiomes)
                    )
                    .apply(instance, BiomePlacementMarshaller::new));

    public void unmarshall() {
        for (AddBiomeMarshaller marshaller : additions) {
            marshaller.unmarshall();
        }
        for (RemoveBiomeMarshaller marshaller : removals) {
            marshaller.unmarshall();
        }
        for (ReplaceBiomeMarshaller marshaller : replacements) {
            marshaller.unmarshall();
        }
        for (AddSubBiomeMarshaller marshaller : subBiomes) {
            marshaller.unmarshall();
        }
    }

    public record AddBiomeMarshaller(ResourceKey<DimensionType> dimension, ResourceKey<Biome> biome, Climate.ParameterPoint noisePoint) {
        public static Codec<AddBiomeMarshaller> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                                ResourceKey.codec(Registries.DIMENSION_TYPE).fieldOf("dimension")
                                        .forGetter(AddBiomeMarshaller::dimension),
                                ResourceKey.codec(Registries.BIOME).fieldOf("biome")
                                        .forGetter(AddBiomeMarshaller::biome),
                                Climate.ParameterPoint.CODEC.fieldOf("noise")
                                        .forGetter(AddBiomeMarshaller::noisePoint)
                        )
                        .apply(instance, AddBiomeMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(BuiltinDimensionTypes.OVERWORLD)) {
                BiomeCoordinator.OVERWORLD.addPlacement(biome, noisePoint, true);
            } else if (dimension.equals(BuiltinDimensionTypes.NETHER)) {
                BiomeCoordinator.NETHER.addPlacement(biome, noisePoint, true);
            } else if (dimension.equals(BuiltinDimensionTypes.END)) {
                BiomeCoordinator.END.addPlacement(biome, noisePoint, true);
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing biome placement.", dimension.identifier());
            }
        }
    }

    public record RemoveBiomeMarshaller(ResourceKey<DimensionType> dimension, ResourceKey<Biome> biome) {
        public static Codec<RemoveBiomeMarshaller> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                                ResourceKey.codec(Registries.DIMENSION_TYPE).fieldOf("dimension")
                                        .forGetter(RemoveBiomeMarshaller::dimension),
                                ResourceKey.codec(Registries.BIOME).fieldOf("biome")
                                        .forGetter(RemoveBiomeMarshaller::biome)
                        )
                        .apply(instance, RemoveBiomeMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(BuiltinDimensionTypes.OVERWORLD)) {
                BiomeCoordinator.OVERWORLD.addRemoval(biome, true);
            } else if (dimension.equals(BuiltinDimensionTypes.NETHER)) {
                BiomeCoordinator.NETHER.addRemoval(biome, true);
            } else if (dimension.equals(BuiltinDimensionTypes.END)) {
                BiomeCoordinator.END.addRemoval(biome, true);
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing biome placement.", dimension.identifier());
            }
        }
    }

    public record ReplaceBiomeMarshaller(ResourceKey<DimensionType> dimension, ResourceKey<Biome> target, ResourceKey<Biome> biome, double proportion) {
        public static Codec<ReplaceBiomeMarshaller> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                                ResourceKey.codec(Registries.DIMENSION_TYPE).fieldOf("dimension")
                                        .forGetter(ReplaceBiomeMarshaller::dimension),
                                ResourceKey.codec(Registries.BIOME).fieldOf("target")
                                        .forGetter(ReplaceBiomeMarshaller::target),
                                ResourceKey.codec(Registries.BIOME).fieldOf("biome")
                                        .forGetter(ReplaceBiomeMarshaller::biome),
                                Codec.DOUBLE.optionalFieldOf("proportion", 1.0d)
                                        .forGetter(ReplaceBiomeMarshaller::proportion)
                        )
                        .apply(instance, ReplaceBiomeMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(BuiltinDimensionTypes.OVERWORLD)) {
                BiomeCoordinator.OVERWORLD.addReplacement(target, biome, proportion, true);
            } else if (dimension.equals(BuiltinDimensionTypes.NETHER)) {
                BiomeCoordinator.NETHER.addReplacement(target, biome, proportion, true);
            } else if (dimension.equals(BuiltinDimensionTypes.END)) {
                BiomeCoordinator.END.addReplacement(target, biome, proportion, true);
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing biome placement.", dimension.identifier());
            }
        }
    }

    public record AddSubBiomeMarshaller(ResourceKey<DimensionType> dimension, ResourceKey<Biome> target, ResourceKey<Biome> biome, Criterion criterion) {
        public static Codec<AddSubBiomeMarshaller> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                                ResourceKey.codec(Registries.DIMENSION_TYPE).fieldOf("dimension")
                                        .forGetter(AddSubBiomeMarshaller::dimension),
                                ResourceKey.codec(Registries.BIOME).fieldOf("target")
                                        .forGetter(AddSubBiomeMarshaller::target),
                                ResourceKey.codec(Registries.BIOME).fieldOf("biome")
                                        .forGetter(AddSubBiomeMarshaller::biome),
                                Criterion.MATCHER_CODEC.fieldOf("criterion")
                                        .forGetter(AddSubBiomeMarshaller::criterion)
                        )
                        .apply(instance, AddSubBiomeMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(BuiltinDimensionTypes.OVERWORLD)) {
                BiomeCoordinator.OVERWORLD.addSubBiome(target, biome, criterion, true);
            } else if (dimension.equals(BuiltinDimensionTypes.NETHER)) {
                BiomeCoordinator.NETHER.addSubBiome(target, biome, criterion, true);
            } else if (dimension.equals(BuiltinDimensionTypes.END)) {
                BiomeCoordinator.END.addSubBiome(target, biome, criterion, true);
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing biome placement.", dimension.identifier());
            }
        }
    }
}
