package com.terraformersmc.biolith.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record SurfaceGenerationMarshaller(List<SurfaceRuleMarshaller> surfaceRules) {
    public static final Codec<SurfaceGenerationMarshaller> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                    SurfaceRuleMarshaller.CODEC.listOf().optionalFieldOf("surface_rules", List.of())
                            .forGetter(SurfaceGenerationMarshaller::surfaceRules)
                    )
                    .apply(instance, SurfaceGenerationMarshaller::new));

    public void unmarshall() {
        for (SurfaceRuleMarshaller surfaceRule : surfaceRules) {
            surfaceRule.unmarshall();
        }
    }

    public record SurfaceRuleMarshaller(ResourceKey<DimensionType> dimension, Identifier rulesOwner, List<SurfaceRules.RuleSource> materialRules) {
        public static Codec<SurfaceRuleMarshaller> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                            ResourceKey.codec(Registries.DIMENSION_TYPE).fieldOf("dimension")
                                    .forGetter(SurfaceRuleMarshaller::dimension),
                            Identifier.CODEC.fieldOf("rules_owner")
                                    .forGetter(SurfaceRuleMarshaller::rulesOwner),
                            SurfaceRules.RuleSource.CODEC.listOf().optionalFieldOf("material_rules", List.of())
                                    .forGetter(SurfaceRuleMarshaller::materialRules)
                    )
                    .apply(instance, SurfaceRuleMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(BuiltinDimensionTypes.OVERWORLD)) {
                SurfaceRuleCollector.OVERWORLD.addFromData(rulesOwner, materialRules.toArray(new SurfaceRules.RuleSource[0]));
            } else if (dimension.equals(BuiltinDimensionTypes.NETHER)) {
                SurfaceRuleCollector.NETHER.addFromData(rulesOwner, materialRules.toArray(new SurfaceRules.RuleSource[0]));
            } else if (dimension.equals(BuiltinDimensionTypes.END)) {
                SurfaceRuleCollector.END.addFromData(rulesOwner, materialRules.toArray(new SurfaceRules.RuleSource[0]));
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing surface generation.", dimension.identifier());
            }
        }
    }
}
