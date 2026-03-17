package com.terraformersmc.biolith.api.biome.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.Optionull;
import net.minecraft.resources.Identifier;

/**
 * CriterionType associates a criterion with its serialization codec, which is used
 * during marshalling and unmarshalling criteria in datapacks.
 *
 * @param <T> must extend {@linkplain Criterion}
 */
public interface CriterionType<T extends Criterion> {
    Codec<CriterionType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id ->
        Optionull.mapOrElse(CriterionTypes.get(id), DataResult::success, () -> DataResult.error(() -> "Unknown criterion type" + id)),
        CriterionType::getId
    );

    MapCodec<T> getCodec();

    Identifier getId();

    /**
     * Create a new criterion type.
     *
     * @param codec {@linkplain Codec} used to serialize the new criterion type
     * @param id {@linkplain Identifier} of the new criterion type
     * @return newly created {@linkplain CriterionType}
     * @param <T> must extend {@linkplain Criterion}
     */
    static <T extends Criterion> CriterionType<T> createType(MapCodec<T> codec, Identifier id) {
        return new CriterionType<>() {
            @Override
            public MapCodec<T> getCodec() {
                return codec;
            }

            @Override
            public Identifier getId() {
                return id;
            }
        };
    }
}
