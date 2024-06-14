package com.terraformersmc.biolith.impl.biome.subbiome;

import com.terraformersmc.biolith.api.biome.subbiome.CriteriaType;
import com.terraformersmc.biolith.api.biome.subbiome.CriteriaTypes;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.util.Identifier;

public class BiolithCriterion {
    protected static CriteriaType<NotCriteria> NOT = CriteriaType.createType(NotCriteria.CODEC, of("not"));
    protected static CriteriaType<AllOfCriteria> ALL_OF = CriteriaType.createType(AllOfCriteria.CODEC, of("all_of"));
    protected static CriteriaType<AnyOfCriteria> ANY_OF = CriteriaType.createType(AnyOfCriteria.CODEC, of("any_of"));
    protected static CriteriaType<CenterDistanceCriteria> CENTER_DISTANCE = CriteriaType.createType(CenterDistanceCriteria.CODEC, of("center_distance"));
    protected static CriteriaType<ValueCriteria> VALUE = CriteriaType.createType(ValueCriteria.CODEC, of("value"));
    public static final CriteriaType<RatioCriteria> RATIO = CriteriaType.createType(RatioCriteria.CODEC, of("ratio"));
    protected static CriteriaType<OriginalCriteria> ORIGINAL = CriteriaType.createType(OriginalCriteria.CODEC, of("original"));
    protected static CriteriaType<NeighborCriteria> NEIGHBOR = CriteriaType.createType(NeighborCriteria.CODEC, of("neighbor"));
    protected static CriteriaType<AlternateCriteria> ALTERNATE = CriteriaType.createType(AlternateCriteria.CODEC, of("alternate"));

    private static Identifier of(String name) {
        return Identifier.of(Biolith.MOD_ID, name);
    }

    public static void init() {
        CriteriaTypes.add(NOT);
        CriteriaTypes.add(ALL_OF);
        CriteriaTypes.add(ANY_OF);
        CriteriaTypes.add(CENTER_DISTANCE);
        CriteriaTypes.add(VALUE);
        CriteriaTypes.add(RATIO);
        CriteriaTypes.add(ORIGINAL);
        CriteriaTypes.add(NEIGHBOR);
        CriteriaTypes.add(ALTERNATE);
    }
}
