package com.terraformersmc.biolith.impl.biome.subbiome;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.api.biome.subbiome.BiomeParameterTarget;
import com.terraformersmc.biolith.api.biome.subbiome.Criteria;
import net.minecraft.util.dynamic.Range;

import java.util.function.BiFunction;

public abstract class AbstractParameterCriteria implements Criteria {
    protected final BiomeParameterTarget parameter;
    protected Range<Float> allowedValues;

    public AbstractParameterCriteria(BiomeParameterTarget parameter, float min, float max) {
        this.parameter = parameter;
        this.allowedValues = new Range<>(min, max);
    }

    protected static <T extends AbstractParameterCriteria> MapCodec<T> buildCodec(Function3<BiomeParameterTarget, Float, Float, T> function) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeParameterTarget.CODEC.fieldOf("parameter").forGetter(AbstractParameterCriteria::parameter),
            Codec.FLOAT.optionalFieldOf("min", -64f).forGetter(AbstractParameterCriteria::min),
            Codec.FLOAT.optionalFieldOf("max", 64f).forGetter(AbstractParameterCriteria::max)
        ).apply(instance, function));
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
