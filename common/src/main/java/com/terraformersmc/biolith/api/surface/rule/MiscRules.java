package com.terraformersmc.biolith.api.surface.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

public class MiscRules {

    public static class Configured implements MaterialRules.MaterialCondition {

        public static final CodecHolder<Configured> CODEC = CodecHolder.of(
                RecordCodecBuilder.mapCodec(instance ->
                        instance.group(
                                Codec.BOOL.fieldOf("pass").forGetter(r -> r.value)
                        ).apply(instance, Configured::new)
                )
        );

        private final boolean value;

        public Configured(boolean value) {
            this.value = value;
        }

        @Override
        public MaterialRules.BooleanSupplier apply(MaterialRules.MaterialRuleContext context) {
            return new Condition(value);
        }

        private static final class Condition implements MaterialRules.BooleanSupplier {

            private final boolean value;

            Condition(boolean value) {
                this.value = value;
            }

            @Override
            public boolean get() {
                return value;
            }
        }

        public static MaterialRules.MaterialCondition pass(boolean value) {
            return new Configured(value);
        }

        @Override
        public CodecHolder<? extends MaterialRules.MaterialCondition> codec() {
            return CODEC;
        }
    }
}
