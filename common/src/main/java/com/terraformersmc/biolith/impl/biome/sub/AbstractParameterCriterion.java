package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.sub.BiomeParameterTargets;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import net.minecraft.util.dynamic.Range;

public abstract class AbstractParameterCriterion implements Criterion {
    protected final BiomeParameterTargets parameter;
    protected final Range<Float> allowedValues;

    public AbstractParameterCriterion(BiomeParameterTargets parameter, float min, float max) {
        this.parameter = parameter;
        this.allowedValues = new Range<>(min, max);
    }

    protected static <T extends AbstractParameterCriterion> MapCodec<T> buildCodec(Function3<BiomeParameterTargets, Float, Float, T> function) {
        return RecordCodecBuilder.mapCodec(
                (instance) -> instance.group(
                                BiomeParameterTargets.CODEC.fieldOf("parameter")
                                        .forGetter(AbstractParameterCriterion::parameter),
                                Codec.FLOAT.optionalFieldOf("min", Float.NEGATIVE_INFINITY)
                                        .forGetter(AbstractParameterCriterion::min),
                                Codec.FLOAT.optionalFieldOf("max", Float.POSITIVE_INFINITY)
                                        .forGetter(AbstractParameterCriterion::max)
                        )
                        .apply(instance, function));
    }

    public BiomeParameterTargets parameter() {
        return parameter;
    }

    public float min() {
        return allowedValues.minInclusive();
    }

    public float max() {
        return allowedValues.maxInclusive();
    }
}
