package com.terraformersmc.biolith.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.subbiome.Criteria;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

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

    public record AddBiomeMarshaller(RegistryKey<DimensionType> dimension, RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube noisePoint) {
        public static Codec<AddBiomeMarshaller> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                        RegistryKey.createCodec(RegistryKeys.DIMENSION_TYPE).fieldOf("dimension")
                                .forGetter(AddBiomeMarshaller::dimension),
                        RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("biome")
                                .forGetter(AddBiomeMarshaller::biome),
                        MultiNoiseUtil.NoiseHypercube.CODEC.fieldOf("noise")
                                .forGetter(AddBiomeMarshaller::noisePoint)
                        )
                        .apply(instance, AddBiomeMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(DimensionTypes.OVERWORLD)) {
                BiomeCoordinator.OVERWORLD.addPlacement(biome, noisePoint, true);
            } else if(dimension.equals(DimensionTypes.THE_NETHER)) {
                BiomeCoordinator.NETHER.addPlacement(biome, noisePoint, true);
            } else if(dimension.equals(DimensionTypes.THE_END)) {
                BiomeCoordinator.END.addPlacement(biome, noisePoint, true);
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing biome placement.", dimension.getValue());
            }
        }
    }

    public record RemoveBiomeMarshaller(RegistryKey<DimensionType> dimension, RegistryKey<Biome> biome) {
        public static Codec<RemoveBiomeMarshaller> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                                RegistryKey.createCodec(RegistryKeys.DIMENSION_TYPE).fieldOf("dimension")
                                        .forGetter(RemoveBiomeMarshaller::dimension),
                                RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("biome")
                                        .forGetter(RemoveBiomeMarshaller::biome)
                        )
                        .apply(instance, RemoveBiomeMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(DimensionTypes.OVERWORLD)) {
                BiomeCoordinator.OVERWORLD.addRemoval(biome, true);
            } else if(dimension.equals(DimensionTypes.THE_NETHER)) {
                BiomeCoordinator.NETHER.addRemoval(biome, true);
            } else if(dimension.equals(DimensionTypes.THE_END)) {
                BiomeCoordinator.END.addRemoval(biome, true);
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing biome placement.", dimension.getValue());
            }
        }
    }

    public record ReplaceBiomeMarshaller(RegistryKey<DimensionType> dimension, RegistryKey<Biome> target, RegistryKey<Biome> biome, double proportion) {
        public static Codec<ReplaceBiomeMarshaller> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                                RegistryKey.createCodec(RegistryKeys.DIMENSION_TYPE).fieldOf("dimension")
                                        .forGetter(ReplaceBiomeMarshaller::dimension),
                                RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("target")
                                        .forGetter(ReplaceBiomeMarshaller::target),
                                RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("biome")
                                        .forGetter(ReplaceBiomeMarshaller::biome),
                                Codec.DOUBLE.optionalFieldOf("proportion", 1.0d)
                                        .forGetter(ReplaceBiomeMarshaller::proportion)
                        )
                        .apply(instance, ReplaceBiomeMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(DimensionTypes.OVERWORLD)) {
                BiomeCoordinator.OVERWORLD.addReplacement(target, biome, proportion, true);
            } else if(dimension.equals(DimensionTypes.THE_NETHER)) {
                BiomeCoordinator.NETHER.addReplacement(target, biome, proportion, true);
            } else if(dimension.equals(DimensionTypes.THE_END)) {
                BiomeCoordinator.END.addReplacement(target, biome, proportion, true);
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing biome placement.", dimension.getValue());
            }
        }
    }

    public record AddSubBiomeMarshaller(RegistryKey<DimensionType> dimension, RegistryKey<Biome> target, RegistryKey<Biome> biome, Criteria criteria) {
        public static Codec<AddSubBiomeMarshaller> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryKey.createCodec(RegistryKeys.DIMENSION_TYPE).fieldOf("dimension").forGetter(AddSubBiomeMarshaller::dimension),
            RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("target").forGetter(AddSubBiomeMarshaller::target),
            RegistryKey.createCodec(RegistryKeys.BIOME).fieldOf("biome").forGetter(AddSubBiomeMarshaller::biome),
            Criteria.CODEC.fieldOf("criteria").forGetter(AddSubBiomeMarshaller::criteria)
        ).apply(instance, AddSubBiomeMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(DimensionTypes.OVERWORLD)) {
                BiomeCoordinator.OVERWORLD.addSubBiome(target, biome, criteria, true);
            } else if (dimension.equals(DimensionTypes.THE_NETHER)) {
                BiomeCoordinator.NETHER.addSubBiome(target, biome, criteria, true);
            } else if (dimension.equals(DimensionTypes.THE_END)) {
                BiomeCoordinator.END.addSubBiome(target, biome, criteria, true);
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing biome placement.", dimension.getValue());
            }
        }
    }
}
