package com.terraformersmc.biolith.impl.surface;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

import java.util.*;

public class SurfaceRuleCollector {
    public static final SurfaceRuleCollector END = new SurfaceRuleCollector();
    public static final SurfaceRuleCollector NETHER = new SurfaceRuleCollector();
    public static final SurfaceRuleCollector OVERWORLD = new SurfaceRuleCollector();

    private final HashMap<Identifier, List<MaterialRules.MaterialRule>> MATERIAL_RULES = new HashMap<>(16);

    public void add(Identifier ruleOwner, MaterialRules.MaterialRule... materialRules) {
        if (materialRules.length > 0) {
            MATERIAL_RULES.computeIfAbsent(ruleOwner, ignored -> new ArrayList<>(4))
                    .addAll(Arrays.stream(materialRules).toList());
        } else {
            Biolith.LOGGER.warn("Request to add empty surface rule set with ID of '{}'", ruleOwner);
        }
    }

    public MaterialRules.MaterialRule get(Identifier ruleOwner) {
        if (MATERIAL_RULES.get(ruleOwner).size() > 1) {
            MaterialRules.MaterialRule[] rules = new MaterialRules.MaterialRule[0];
            return MaterialRules.sequence(MATERIAL_RULES.get(ruleOwner).toArray(rules));
        } else {
            return MATERIAL_RULES.get(ruleOwner).get(0);
        }
    }

    public MaterialRules.MaterialRule[] getAll() {
        MaterialRules.MaterialRule[] rulesType = new MaterialRules.MaterialRule[0];
        return MATERIAL_RULES.keySet().stream()
                .map(this::get)
                .toList().toArray(rulesType);
    }

    public Set<Identifier> getRuleOwners() {
        return MATERIAL_RULES.keySet();
    }

    public int getRuleCount() {
        return MATERIAL_RULES.size();
    }

    public int getRuleCount(Identifier ruleOwner) {
        if (MATERIAL_RULES.containsKey(ruleOwner)) {
            return MATERIAL_RULES.get(ruleOwner).size();
        }

        return 0;
    }
}
