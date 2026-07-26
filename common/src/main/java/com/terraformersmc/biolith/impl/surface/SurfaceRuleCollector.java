package com.terraformersmc.biolith.impl.surface;

import com.google.common.collect.Sets;
import com.terraformersmc.biolith.api.surface.RuleSourceBootstrapper;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SurfaceRuleCollector {
    public static final SurfaceRuleCollector END = new SurfaceRuleCollector();
    public static final SurfaceRuleCollector NETHER = new SurfaceRuleCollector();
    public static final SurfaceRuleCollector OVERWORLD = new SurfaceRuleCollector();

    private final HashMap<Identifier, List<SurfaceRules.RuleSource>> MATERIAL_RULES_DATA = new HashMap<>(16);
    private final HashMap<Identifier, List<RuleSourceBootstrapper>> MATERIAL_RULES_MODS = new HashMap<>(16);

    public void addFromData(Identifier ruleOwner, SurfaceRules.RuleSource... materialRules) {
        if (materialRules.length > 0) {
            MATERIAL_RULES_DATA.computeIfAbsent(ruleOwner, ignored -> new ArrayList<>(4))
                    .addAll(Arrays.stream(materialRules).toList());
        } else {
            Biolith.LOGGER.warn("Request by data to add empty surface rule set with ID of '{}'", ruleOwner);
        }
    }

    public void addFromMods(Identifier ruleOwner, RuleSourceBootstrapper... materialRules) {
        if (materialRules.length > 0) {
            MATERIAL_RULES_MODS.computeIfAbsent(ruleOwner, ignored -> new ArrayList<>(4))
                    .addAll(Arrays.stream(materialRules).toList());
        } else {
            Biolith.LOGGER.warn("Request by mod to add empty surface rule set with ID of '{}'", ruleOwner);
        }
    }

    public void clearFromData() {
        MATERIAL_RULES_DATA.clear();
    }

    public void clearFromMods() {
        MATERIAL_RULES_MODS.clear();
    }

    public @Nullable List<RuleSourceBootstrapper> clearFromMod(Identifier ruleOwner) {
        return MATERIAL_RULES_MODS.remove(ruleOwner);
    }

    public @Nullable RuleSourceBootstrapper get(Identifier ruleOwner) {
        if (MATERIAL_RULES_DATA.containsKey(ruleOwner)) {
            if (MATERIAL_RULES_DATA.get(ruleOwner).size() > 1) {
                return new SequencedRuleSourceBootstrapper(MATERIAL_RULES_DATA.get(ruleOwner).stream()
                        .map(rule -> RuleSourceBootstrapper.cast((_) -> rule))
                        .toArray(RuleSourceBootstrapper[]::new));
            } else {
                return (_) -> MATERIAL_RULES_DATA.get(ruleOwner).getFirst();
            }
        } else if (MATERIAL_RULES_MODS.containsKey(ruleOwner)) {
            if (MATERIAL_RULES_MODS.get(ruleOwner).size() > 1) {
                return new SequencedRuleSourceBootstrapper(MATERIAL_RULES_MODS.get(ruleOwner)
                        .toArray(RuleSourceBootstrapper[]::new));
            } else {
                return MATERIAL_RULES_MODS.get(ruleOwner).getFirst();
            }
        }

        return null;
    }

    private SurfaceRules.RuleSource getFromData(Identifier ruleOwner) {
        if (MATERIAL_RULES_DATA.get(ruleOwner).size() > 1) {
            return SurfaceRules.sequence(MATERIAL_RULES_DATA.get(ruleOwner)
                    .toArray(SurfaceRules.RuleSource[]::new));
        }

        return MATERIAL_RULES_DATA.get(ruleOwner).getFirst();
    }

    private RuleSourceBootstrapper getFromMods(Identifier ruleOwner) {
        if (MATERIAL_RULES_MODS.get(ruleOwner).size() > 1) {
            return new SequencedRuleSourceBootstrapper(MATERIAL_RULES_MODS.get(ruleOwner)
                    .toArray(RuleSourceBootstrapper[]::new));
        }

        return MATERIAL_RULES_MODS.get(ruleOwner).getFirst();
    }

    // Get all wrapped rule sources
    public RuleSourceBootstrapper[] getAll() {
        return getRuleOwners().stream()
                .map((key) -> MATERIAL_RULES_DATA.containsKey(key) ? RuleSourceBootstrapper.cast((_) -> getFromData(key)) : getFromMods(key))
                .toArray(RuleSourceBootstrapper[]::new);
    }

    // Get all finalized rule sources
    public SurfaceRules.RuleSource[] getAllBootstrapped(HolderGetter<Biome> biomeGetter) {
        return getRuleOwners().stream()
                .map((key) -> MATERIAL_RULES_DATA.containsKey(key) ? getFromData(key) : getFromMods(key).apply(biomeGetter))
                .toArray(SurfaceRules.RuleSource[]::new);
    }

    public Set<Identifier> getRuleOwners() {
        return Sets.union(MATERIAL_RULES_DATA.keySet(), MATERIAL_RULES_MODS.keySet());
    }

    public int getRuleCount() {
        return getRuleOwners().size();
    }
}
