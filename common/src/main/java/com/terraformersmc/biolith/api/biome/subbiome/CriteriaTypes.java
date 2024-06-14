package com.terraformersmc.biolith.api.biome.subbiome;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CriteriaTypes {
    private static final Map<Identifier, CriteriaType<?>> CRITERIA_TYPES = new ConcurrentHashMap<>();

    public static CriteriaType<?> get(Identifier id) {
        return CRITERIA_TYPES.get(id);
    }

    public static <T extends Criteria> CriteriaType<T> add(CriteriaType<T> type) {
        if (CRITERIA_TYPES.containsKey(type.getId())) {
            Biolith.LOGGER.error("Cannot register two criteria types with same id: " + type.getId());
            return null;
        }

        CRITERIA_TYPES.put(type.getId(), type);
        return type;
    }
}
