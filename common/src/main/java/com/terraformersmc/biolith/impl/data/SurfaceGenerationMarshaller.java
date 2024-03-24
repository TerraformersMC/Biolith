package com.terraformersmc.biolith.impl.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

import java.util.List;

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

    public record SurfaceRuleMarshaller(RegistryKey<DimensionType> dimension, Identifier rulesOwner, List<MaterialRules.MaterialRule> materialRules) {
        public static Codec<SurfaceRuleMarshaller> CODEC = RecordCodecBuilder.create(
            (instance) -> instance.group(
                            RegistryKey.createCodec(RegistryKeys.DIMENSION_TYPE).fieldOf("dimension")
                                    .forGetter(SurfaceRuleMarshaller::dimension),
                            Identifier.CODEC.fieldOf("rules_owner")
                                    .forGetter(SurfaceRuleMarshaller::rulesOwner),
                            MaterialRules.MaterialRule.CODEC.listOf().optionalFieldOf("material_rules", List.of())
                                    .forGetter(SurfaceRuleMarshaller::materialRules)
                    )
                    .apply(instance, SurfaceRuleMarshaller::new));

        public void unmarshall() {
            if (dimension.equals(DimensionTypes.OVERWORLD)) {
                SurfaceRuleCollector.OVERWORLD.addFromData(rulesOwner, materialRules.toArray(new MaterialRules.MaterialRule[0]));
            } else if (dimension.equals(DimensionTypes.THE_NETHER)) {
                SurfaceRuleCollector.NETHER.addFromData(rulesOwner, materialRules.toArray(new MaterialRules.MaterialRule[0]));
            } else if (dimension.equals(DimensionTypes.THE_END)) {
                SurfaceRuleCollector.END.addFromData(rulesOwner, materialRules.toArray(new MaterialRules.MaterialRule[0]));
            } else {
                Biolith.LOGGER.warn("Ignored unknown dimension type '{}' while serializing surface generation.", dimension.getValue());
            }
        }
    }
}
