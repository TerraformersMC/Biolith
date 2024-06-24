package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class AbstractBiomeCriterion implements Criterion {
    protected final BiomeTarget biomeTarget;

    public AbstractBiomeCriterion(BiomeTarget biomeTarget) {
        this.biomeTarget = biomeTarget;
    }

    protected static <T extends AbstractBiomeCriterion> MapCodec<T> buildCodec(Function<BiomeTarget, T> function) {
        return RecordCodecBuilder.mapCodec(
                (instance) -> instance.group(
                                BiomeTarget.CODEC.fieldOf("biome")
                                        .forGetter(AbstractBiomeCriterion::biomeTarget)
                        )
                        .apply(instance, function));
    }

    public BiomeTarget biomeTarget() {
        return biomeTarget;
    }

    public record BiomeTarget(@Nullable RegistryKey<Biome> biome, @Nullable TagKey<Biome> tag) {
        public static final Codec<BiomeTarget> CODEC = Codec.either(
                        RegistryKey.createCodec(RegistryKeys.BIOME),
                        TagKey.codec(RegistryKeys.BIOME)
                )
                .flatComapMap(BiomeTarget::fromEither, BiomeTarget::toEither);

        public BiomeTarget {
            if (biome == null && tag == null) {
                throw new IllegalArgumentException("Must specify either biome or tag!");
            }
        }

        public static BiomeTarget of(@NotNull RegistryKey<Biome> biome) {
            return new BiomeTarget(biome, null);
        }

        public static BiomeTarget of(@NotNull TagKey<Biome> tag) {
            return new BiomeTarget(null, tag);
        }

        public static BiomeTarget fromEither(Either<RegistryKey<Biome>, TagKey<Biome>> either) {
            return new BiomeTarget(either.left().orElse(null), either.right().orElse(null));
        }

        public DataResult<Either<RegistryKey<Biome>, TagKey<Biome>>> toEither() {
            return DataResult.success(this.biome != null ?
                    Either.left(this.biome) :
                    Either.right(this.tag)
            );
        }

        public boolean matches(@Nullable RegistryEntry<Biome> biome) {
            if (biome == null) {
                return false;
            }

            if (this.biome != null && biome.matchesKey(this.biome)) {
                return true;
            }

            return (this.tag != null && biome.isIn(this.tag));
        }
    }
}
