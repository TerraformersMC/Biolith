package com.terraformersmc.biolith.impl.biome.subbiome;

import com.terraformersmc.biolith.api.biome.subbiome.CriterionType;
import com.terraformersmc.biolith.api.biome.subbiome.CriterionTypes;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.util.Identifier;

public class BiolithCriterion {
    protected static CriterionType<NotCriterion> NOT = CriterionType.createType(NotCriterion.CODEC, of("not"));
    protected static CriterionType<AllOfCriterion> ALL_OF = CriterionType.createType(AllOfCriterion.CODEC, of("all_of"));
    protected static CriterionType<AnyOfCriterion> ANY_OF = CriterionType.createType(AnyOfCriterion.CODEC, of("any_of"));
    protected static CriterionType<CenterDistanceCriterion> CENTER_DISTANCE = CriterionType.createType(CenterDistanceCriterion.CODEC, of("center_distance"));
    protected static CriterionType<ValueCriterion> VALUE = CriterionType.createType(ValueCriterion.CODEC, of("value"));
    public static final CriterionType<RatioCriterion> RATIO = CriterionType.createType(RatioCriterion.CODEC, of("ratio"));
    protected static CriterionType<OriginalCriterion> ORIGINAL = CriterionType.createType(OriginalCriterion.CODEC, of("original"));
    protected static CriterionType<NeighborCriterion> NEIGHBOR = CriterionType.createType(NeighborCriterion.CODEC, of("neighbor"));
    protected static CriterionType<AlternateCriterion> ALTERNATE = CriterionType.createType(AlternateCriterion.CODEC, of("alternate"));

    private static Identifier of(String name) {
        return Identifier.of(Biolith.MOD_ID, name);
    }

    public static void init() {
        CriterionTypes.add(NOT);
        CriterionTypes.add(ALL_OF);
        CriterionTypes.add(ANY_OF);
        CriterionTypes.add(CENTER_DISTANCE);
        CriterionTypes.add(VALUE);
        CriterionTypes.add(RATIO);
        CriterionTypes.add(ORIGINAL);
        CriterionTypes.add(NEIGHBOR);
        CriterionTypes.add(ALTERNATE);
    }
}
