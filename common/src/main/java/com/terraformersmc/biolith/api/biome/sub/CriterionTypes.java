package com.terraformersmc.biolith.api.biome.sub;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of available {@linkplain CriterionType criterion types}.  All {@linkplain Criterion criteria}
 * must be associated with a type registered here in order for them to be available via the data API.
 */
public class CriterionTypes {
    private static final Map<Identifier, CriterionType<?>> CRITERION_TYPES = new HashMap<>();

    /**
     * Fetch a registered criterion type by ID.
     *
     * @param id {@linkplain Identifier} of the CriterionType
     * @return Registered {@linkplain CriterionType}, or null if the ID has not been registered
     */
    public static synchronized @Nullable CriterionType<?> get(Identifier id) {
        return CRITERION_TYPES.get(id);
    }

    /**
     * Register a new criterion type.  Duplicate registrations (by ID) will be rejected.
     *
     * @param type {@linkplain CriterionType} to be registered
     * @return Registered CriterionType, or null if the registration was rejected
     * @param <T> must extend {@linkplain Criterion}
     */
    public static synchronized <T extends Criterion> @Nullable CriterionType<T> add(CriterionType<T> type) {
        if (CRITERION_TYPES.containsKey(type.getId())) {
            Biolith.LOGGER.error("Cannot register two criterion types with same id: {}", type.getId());

            return null;
        }

        CRITERION_TYPES.put(type.getId(), type);

        return type;
    }
}
