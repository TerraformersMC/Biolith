package com.terraformersmc.biolith.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.SubBiomeMatcher;
import com.terraformersmc.biolith.impl.biome.SubBiomeMatcherImpl;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public record SubBiomeMatcherMarshaller(List<SubBiomeCriterionMarshaller> criteria) {
    public static Codec<SubBiomeMatcherMarshaller> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    Codecs.nonEmptyList(SubBiomeCriterionMarshaller.CODEC.listOf()).fieldOf("criteria")
                            .forGetter(SubBiomeMatcherMarshaller::criteria)
                    )
                    .apply(instance, SubBiomeMatcherMarshaller::new));

    public SubBiomeMatcher unmarshall() {
        return SubBiomeMatcherImpl.of(criteria.stream()
                .map(SubBiomeCriterionMarshaller::unmarshall)
                .toArray(SubBiomeMatcher.Criterion[]::new));
    }

    public record SubBiomeCriterionMarshaller(SubBiomeMatcher.CriterionTargets target, SubBiomeMatcher.CriterionTypes type, RegistryKey<Biome> biome, RegistryKey<Biome> secondary, TagKey<Biome> biomeTag, float min, float max, boolean invert) {
        public static Codec<SubBiomeCriterionMarshaller> CODEC = RecordCodecBuilder.create(
                (instance) -> instance.group(
                        Codec.STRING.fieldOf("target")
                                .forGetter((marshaller) -> marshaller.target.name()),
                        Codec.STRING.fieldOf("type")
                                .forGetter((marshaller) -> marshaller.type.name()),
                        RegistryKey.createCodec(RegistryKeys.BIOME).optionalFieldOf("biome")
                                .forGetter((marshaller) -> Optional.of(marshaller.biome)),
                        RegistryKey.createCodec(RegistryKeys.BIOME).optionalFieldOf("secondary")
                                .forGetter((marshaller) -> Optional.of(marshaller.secondary)),
                        TagKey.codec(RegistryKeys.BIOME).optionalFieldOf("biome_tag")
                                .forGetter((marshaller) -> Optional.of(marshaller.biomeTag)),
                        Codec.FLOAT.optionalFieldOf("min", Float.MIN_VALUE)
                                .forGetter(SubBiomeCriterionMarshaller::min),
                        Codec.FLOAT.optionalFieldOf("max", Float.MAX_VALUE)
                                .forGetter(SubBiomeCriterionMarshaller::max),
                        Codec.BOOL.optionalFieldOf("invert", false)
                                .forGetter(SubBiomeCriterionMarshaller::invert)
                        )
                        .apply(instance, (target, type, biome, secondary, biomeTag, min, max, invert) ->
                                new SubBiomeCriterionMarshaller(
                                        SubBiomeMatcher.CriterionTargets.valueOf(target.toUpperCase()),
                                        SubBiomeMatcher.CriterionTypes.valueOf(type.toUpperCase()),
                                        biome.orElse(null),
                                        secondary.orElse(null),
                                        biomeTag.orElse(null),
                                        min,
                                        max,
                                        invert)));

        public SubBiomeMatcher.Criterion unmarshall() {
            return new SubBiomeMatcherImpl.Criterion(target, type, biome, secondary, biomeTag, min, max, invert);
        }
    }
}
