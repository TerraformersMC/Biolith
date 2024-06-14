package com.terraformersmc.biolith.api.biome.subbiome;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CriterionTypes {
    private static final Map<Identifier, CriterionType<?>> CRITERION_TYPES = new ConcurrentHashMap<>();

    protected static CriterionType<?> get(Identifier id) {
        return CRITERION_TYPES.get(id);
    }

    public static <T extends Criterion> CriterionType<T> add(CriterionType<T> type) {
        if (CRITERION_TYPES.containsKey(type.getId())) {
            Biolith.LOGGER.error("Cannot register two criterion types with same id: " + type.getId());
            return null;
        }

        CRITERION_TYPES.put(type.getId(), type);
        return type;
    }
}
