package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.subbiome.Criteria;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;

import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractBiomeCriteria implements Criteria {
    protected final BiomeTarget biomeTarget;

    public AbstractBiomeCriteria(BiomeTarget biomeTarget) {
        this.biomeTarget = biomeTarget;
    }

    protected static <T extends AbstractBiomeCriteria> MapCodec<T> buildCodec(Function<BiomeTarget, T> function) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeTarget.CODEC.fieldOf("biome_target").forGetter(AbstractBiomeCriteria::biomeTarget)
        ).apply(instance, function));
    }

    public BiomeTarget biomeTarget() {
        return biomeTarget;
    }
    public record BiomeTarget(Optional<RegistryKey<Biome>> key, Optional<TagKey<Biome>> tag) {
        public static final Codec<BiomeTarget> CODEC = Codec.either(RegistryKey.createCodec(RegistryKeys.BIOME), TagKey.codec(RegistryKeys.BIOME)).flatComapMap(BiomeTarget::new, BiomeTarget::toEither);

        public BiomeTarget(Either<RegistryKey<Biome>, TagKey<Biome>> either) {
            this(either.left(), either.right());
        }

        public static BiomeTarget ofKey(RegistryKey<Biome> key) {
            return new BiomeTarget(Either.left(key));
        }

        public static BiomeTarget ofTag(TagKey<Biome> tag) {
            return new BiomeTarget(Either.right(tag));
        }

        public boolean matches(RegistryEntry<Biome> biome) {
            return key.map(biome::matchesKey).orElseGet(() -> biome.isIn(tag.get()));
        }

        @SuppressWarnings("OptionalIsPresent")
        public DataResult<Either<RegistryKey<Biome>, TagKey<Biome>>> toEither() {
            return DataResult.success(key.isPresent() ? Either.left(key.get()) : Either.right(tag.get()));
        }
    }
}
