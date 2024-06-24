package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.sub.BiomeParameterTarget;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import net.minecraft.util.dynamic.Range;

public abstract class AbstractParameterCriterion implements Criterion {
    protected final BiomeParameterTarget parameter;
    protected Range<Float> allowedValues;

    public AbstractParameterCriterion(BiomeParameterTarget parameter, float min, float max) {
        this.parameter = parameter;
        this.allowedValues = new Range<>(min, max);
    }

    protected static <T extends AbstractParameterCriterion> MapCodec<T> buildCodec(Function3<BiomeParameterTarget, Float, Float, T> function) {
        return RecordCodecBuilder.mapCodec(
                (instance) -> instance.group(
                                BiomeParameterTarget.CODEC.fieldOf("parameter")
                                        .forGetter(AbstractParameterCriterion::parameter),
                                Codec.FLOAT.optionalFieldOf("min", Float.NEGATIVE_INFINITY)
                                        .forGetter(AbstractParameterCriterion::min),
                                Codec.FLOAT.optionalFieldOf("max", Float.POSITIVE_INFINITY)
                                        .forGetter(AbstractParameterCriterion::max)
                        )
                        .apply(instance, function));
    }

    public BiomeParameterTarget parameter() {
        return parameter;
    }

    public float min() {
        return allowedValues.minInclusive();
    }

    public float max() {
        return allowedValues.maxInclusive();
    }
}
