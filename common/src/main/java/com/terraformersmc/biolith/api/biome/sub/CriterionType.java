package com.terraformersmc.biolith.api.biome.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;

public interface CriterionType<T extends Criterion> {
    Codec<CriterionType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id ->
        Nullables.mapOrElseGet(CriterionTypes.get(id), DataResult::success, () -> DataResult.error(() -> "Unknown criterion type" + id)),
        CriterionType::getId
    );

    MapCodec<T> getCodec();

    Identifier getId();

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
