package com.terraformersmc.biolith.api.surface.rule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class MiscRules {

    public static class Configured implements SurfaceRules.ConditionSource {

        public static final KeyDispatchDataCodec<Configured> CODEC = KeyDispatchDataCodec.of(
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
        public SurfaceRules.Condition  apply(SurfaceRules.Context context) {
            return new Condition(value);
        }

        private static final class Condition implements SurfaceRules.Condition  {

            private final boolean value;

            Condition(boolean value) {
                this.value = value;
            }

            @Override
            public boolean test() {
                return value;
            }
        }

        public static SurfaceRules.ConditionSource pass(boolean value) {
            return new Configured(value);
        }

        @Override
        public KeyDispatchDataCodec<? extends SurfaceRules.ConditionSource> codec() {
            return CODEC;
        }
    }
}
