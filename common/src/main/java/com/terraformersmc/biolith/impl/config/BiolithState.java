package com.terraformersmc.biolith.impl.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.biome.Biome;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class BiolithState extends PersistentState {
    private final LinkedHashMap<RegistryKey<Biome>, LinkedHashSet<RegistryKey<Biome>>> biomeReplacements = new LinkedHashMap<>(64);
    private final ServerWorld world;

    private static <E> Codec<LinkedHashSet<E>> getLinkedHashSetCodec(Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(LinkedHashSet::new, lhs -> lhs.stream().toList());
    }

    public static Codec<BiolithState> getCodec(PersistentState.Context context) {
        return RecordCodecBuilder.create(
                (instance) -> instance.group(
                                Codec.unboundedMap(RegistryKey.createCodec(RegistryKeys.BIOME), getLinkedHashSetCodec(RegistryKey.createCodec(RegistryKeys.BIOME))).optionalFieldOf("biome_replacements", Map.of())
                                        .forGetter(biolithState -> biolithState.biomeReplacements),
                                getLinkedHashSetCodec(RegistryKey.createCodec(RegistryKeys.BIOME)).listOf().optionalFieldOf("BiomeReplacementsList", List.of())
                                        .forGetter(biolithState -> List.of())
                        )
                        .apply(instance, (replacements, biomeReplacementsList) -> {
                            if (!replacements.isEmpty()) {
                                return unmarshall_v1(context, replacements);
                            } else if (!biomeReplacementsList.isEmpty()) {
                                return unmarshall_v0(context, biomeReplacementsList);
                            } else {
                                return new BiolithState(context);
                            }
                        }));
    }

    public static PersistentStateType<BiolithState> getPersistentStateType(String name) {
        return new PersistentStateType<>(
                Biolith.MOD_ID + "_" + name + "_state",
                BiolithState::new,
                BiolithState::getCodec,
                null
        );
    }

    public BiolithState(PersistentState.Context context) {
        this.world = context.getWorldOrThrow();
    }

    // Legacy unmarshaller for upgrading from v0 to v1
    // Each map was stored as a flat ordered list with the key in position 0.
    private static BiolithState unmarshall_v0(PersistentState.Context context, List<LinkedHashSet<RegistryKey<Biome>>> biomeReplacementsList) {
        BiolithState state = new BiolithState(context);

        state.biomeReplacements.clear();
        biomeReplacementsList.forEach(list -> {
            RegistryKey<Biome> key = list.removeFirst();
            state.biomeReplacements.put(key, new LinkedHashSet<>(list));
        });

        // Re-write the state in v1 format
        state.markDirty();

        return state;
    }

    private static BiolithState unmarshall_v1(PersistentState.Context context, Map<RegistryKey<Biome>, LinkedHashSet<RegistryKey<Biome>>> replacements) {
        BiolithState state = new BiolithState(context);

        state.biomeReplacements.clear();
        state.biomeReplacements.putAll(replacements);

        return state;
    }

    public void write() {
        this.markDirty();
        world.getPersistentStateManager().save();
    }

    public Stream<RegistryKey<Biome>> getBiomeReplacements(RegistryKey<Biome> target) {
        if (biomeReplacements.containsKey(target)) {
            return biomeReplacements.get(target).stream();
        } else {
            return Stream.empty();
        }
    }

    public void addBiomeReplacements(RegistryKey<Biome> target, Stream<RegistryKey<Biome>> replacements) {
        if (biomeReplacements.containsKey(target)) {
            replacements.forEachOrdered(biomeReplacements.get(target)::add);
        } else {
            biomeReplacements.put(target, replacements.collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        this.markDirty();
    }

    public Identifier getDimensionId() {
        if (world.getDimensionEntry().getKey().isEmpty()) {
            return Identifier.of("biolith", "unregistered_dimension");
        }

        return world.getDimensionEntry().getKey().get().getValue();
    }

    public Identifier getWorldId() {
        return world.getRegistryKey().getValue();
    }
}
