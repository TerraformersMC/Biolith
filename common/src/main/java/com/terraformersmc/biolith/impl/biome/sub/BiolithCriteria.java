package com.terraformersmc.biolith.impl.biome.sub;

import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.sub.Criterion;
import com.terraformersmc.biolith.api.biome.sub.CriterionType;
import com.terraformersmc.biolith.api.biome.sub.CriterionTypes;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.util.Identifier;

public final class BiolithCriteria {
    static final CriterionType<NotCriterion> NOT = register(NotCriterion.CODEC, "not");
    static final CriterionType<AllOfCriterion> ALL_OF = register(AllOfCriterion.CODEC, "all_of");
    static final CriterionType<AnyOfCriterion> ANY_OF = register(AnyOfCriterion.CODEC, "any_of");
    static final CriterionType<DeviationCriterion> DEVIATION = register(DeviationCriterion.CODEC, "deviation");
    static final CriterionType<ValueCriterion> VALUE = register(ValueCriterion.CODEC, "value");
    static final CriterionType<RatioCriterion> RATIO = register(RatioCriterion.CODEC, "ratio");
    static final CriterionType<OriginalCriterion> ORIGINAL = register(OriginalCriterion.CODEC, "original");
    static final CriterionType<NeighborCriterion> NEIGHBOR = register(NeighborCriterion.CODEC, "neighbor");
    static final CriterionType<AlternateCriterion> ALTERNATE = register(AlternateCriterion.CODEC, "alternate");

    private static <T extends Criterion> CriterionType<T> register(MapCodec<T> codec, String name) {
        return CriterionTypes.add(CriterionType.createType(codec, Identifier.of(Biolith.MOD_ID, name)));
    }

    public static void init() {}
}
