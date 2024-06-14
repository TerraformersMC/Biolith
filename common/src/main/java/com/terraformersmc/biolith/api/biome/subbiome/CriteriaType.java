package com.terraformersmc.biolith.api.biome.subbiome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;

public interface CriteriaType<T extends Criteria> {
    Codec<CriteriaType<?>> TYPE_CODEC = Identifier.CODEC.comapFlatMap(id ->
        Nullables.mapOrElseGet(CriteriaTypes.get(id), DataResult::success, () -> DataResult.error(() -> "Unknown criteria type" + id)),
        CriteriaType::getId
    );

    MapCodec<T> getCodec();

    Identifier getId();

    static <T extends Criteria> CriteriaType<T> createType(MapCodec<T> codec, Identifier id) {
        return new CriteriaType<>() {
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
