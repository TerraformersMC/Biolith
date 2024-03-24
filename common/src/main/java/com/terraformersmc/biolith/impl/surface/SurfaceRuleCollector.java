package com.terraformersmc.biolith.impl.surface;

import com.google.common.collect.Sets;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SurfaceRuleCollector {
    public static final SurfaceRuleCollector END = new SurfaceRuleCollector();
    public static final SurfaceRuleCollector NETHER = new SurfaceRuleCollector();
    public static final SurfaceRuleCollector OVERWORLD = new SurfaceRuleCollector();

    private final HashMap<Identifier, List<MaterialRules.MaterialRule>> MATERIAL_RULES_DATA = new HashMap<>(16);
    private final HashMap<Identifier, List<MaterialRules.MaterialRule>> MATERIAL_RULES_MODS = new HashMap<>(16);

    public void addFromData(Identifier ruleOwner, MaterialRules.MaterialRule... materialRules) {
        if (materialRules.length > 0) {
            MATERIAL_RULES_DATA.computeIfAbsent(ruleOwner, ignored -> new ArrayList<>(4))
                    .addAll(Arrays.stream(materialRules).toList());
        } else {
            Biolith.LOGGER.warn("Request to add empty surface rule set with ID of '{}'", ruleOwner);
        }
    }

    public void addFromMods(Identifier ruleOwner, MaterialRules.MaterialRule... materialRules) {
        if (materialRules.length > 0) {
            MATERIAL_RULES_MODS.computeIfAbsent(ruleOwner, ignored -> new ArrayList<>(4))
                    .addAll(Arrays.stream(materialRules).toList());
        } else {
            Biolith.LOGGER.warn("Request to add empty surface rule set with ID of '{}'", ruleOwner);
        }
    }

    public void clearFromData() {
        MATERIAL_RULES_DATA.clear();
    }

    public void clearFromMods() {
        MATERIAL_RULES_MODS.clear();
    }

    public @Nullable MaterialRules.MaterialRule get(Identifier ruleOwner) {
        if (MATERIAL_RULES_DATA.containsKey(ruleOwner)) {
            if (MATERIAL_RULES_DATA.get(ruleOwner).size() > 1) {
                MaterialRules.MaterialRule[] rules = new MaterialRules.MaterialRule[0];
                return MaterialRules.sequence(MATERIAL_RULES_DATA.get(ruleOwner).toArray(rules));
            } else {
                return MATERIAL_RULES_DATA.get(ruleOwner).get(0);
            }
        } else if (MATERIAL_RULES_MODS.containsKey(ruleOwner)) {
            if (MATERIAL_RULES_MODS.get(ruleOwner).size() > 1) {
                MaterialRules.MaterialRule[] rules = new MaterialRules.MaterialRule[0];
                return MaterialRules.sequence(MATERIAL_RULES_MODS.get(ruleOwner).toArray(rules));
            } else {
                return MATERIAL_RULES_MODS.get(ruleOwner).get(0);
            }
        }

        return null;
    }

    private MaterialRules.MaterialRule getFromData(Identifier ruleOwner) {
        if (MATERIAL_RULES_DATA.get(ruleOwner).size() > 1) {
            MaterialRules.MaterialRule[] rules = new MaterialRules.MaterialRule[0];
            return MaterialRules.sequence(MATERIAL_RULES_DATA.get(ruleOwner).toArray(rules));
        }

        return MATERIAL_RULES_DATA.get(ruleOwner).get(0);
    }

    private MaterialRules.MaterialRule getFromMods(Identifier ruleOwner) {
        if (MATERIAL_RULES_MODS.get(ruleOwner).size() > 1) {
            MaterialRules.MaterialRule[] rules = new MaterialRules.MaterialRule[0];
            return MaterialRules.sequence(MATERIAL_RULES_MODS.get(ruleOwner).toArray(rules));
        }

        return MATERIAL_RULES_MODS.get(ruleOwner).get(0);
    }

    public MaterialRules.MaterialRule[] getAll() {
        MaterialRules.MaterialRule[] rulesType = new MaterialRules.MaterialRule[0];
        return getRuleOwners().stream()
                .map((key) -> MATERIAL_RULES_DATA.containsKey(key) ? getFromData(key) : getFromMods(key))
                .toList().toArray(rulesType);
    }

    public Set<Identifier> getRuleOwners() {
        return Sets.union(MATERIAL_RULES_DATA.keySet(), MATERIAL_RULES_MODS.keySet());
    }

    public int getRuleCount() {
        return getRuleOwners().size();
    }
}
