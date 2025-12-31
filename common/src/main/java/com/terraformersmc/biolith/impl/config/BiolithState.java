package com.terraformersmc.biolith.impl.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraformersmc.biolith.impl.Biolith;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

@SuppressWarnings("unused")
public class BiolithState extends SavedData {
    private final LinkedHashMap<ResourceKey<Biome>, LinkedHashSet<ResourceKey<Biome>>> biomeReplacements = new LinkedHashMap<>(64);
    private final ServerLevel world;

    private static <E> Codec<LinkedHashSet<E>> getLinkedHashSetCodec(Codec<E> entryCodec) {
        return entryCodec.listOf().xmap(LinkedHashSet::new, lhs -> lhs.stream().toList());
    }

    public static Codec<BiolithState> getCodec(ServerLevel world) {
        return RecordCodecBuilder.create(
                (instance) -> instance.group(
                                Codec.unboundedMap(ResourceKey.codec(Registries.BIOME), getLinkedHashSetCodec(ResourceKey.codec(Registries.BIOME))).optionalFieldOf("biome_replacements", Map.of())
                                        .forGetter(biolithState -> biolithState.biomeReplacements),
                                getLinkedHashSetCodec(ResourceKey.codec(Registries.BIOME)).listOf().optionalFieldOf("BiomeReplacementsList", List.of())
                                        .forGetter(biolithState -> List.of())
                        )
                        .apply(instance, (replacements, biomeReplacementsList) -> {
                            if (!replacements.isEmpty()) {
                                return unmarshall_v1(world, replacements);
                            } else if (!biomeReplacementsList.isEmpty()) {
                                return unmarshall_v0(world, biomeReplacementsList);
                            } else {
                                return new BiolithState(world);
                            }
                        }));
    }

    public static SavedDataType<BiolithState> getPersistentStateType(ServerLevel world) {
        return new SavedDataType<>(
                Biolith.MOD_ID + "_state__" + world.dimension().identifier().toDebugFileName(),
                () -> new BiolithState(world),
                BiolithState.getCodec(world),
                null
        );
    }

    public BiolithState(ServerLevel world) {
        this.world = world;
    }

    // Legacy unmarshaller for upgrading from v0 to v1
    // Each map was stored as a flat ordered list with the key in position 0.
    private static BiolithState unmarshall_v0(ServerLevel world, List<LinkedHashSet<ResourceKey<Biome>>> biomeReplacementsList) {
        BiolithState state = new BiolithState(world);

        state.biomeReplacements.clear();
        biomeReplacementsList.forEach(list -> {
            ResourceKey<Biome> key = list.removeFirst();
            state.biomeReplacements.put(key, new LinkedHashSet<>(list));
        });

        // Re-write the state in v1 format
        state.setDirty();

        return state;
    }

    private static BiolithState unmarshall_v1(ServerLevel world, Map<ResourceKey<Biome>, LinkedHashSet<ResourceKey<Biome>>> replacements) {
        BiolithState state = new BiolithState(world);

        state.biomeReplacements.clear();
        state.biomeReplacements.putAll(replacements);

        return state;
    }

    public void write() {
        this.setDirty();
        world.getDataStorage().saveAndJoin();
    }

    public Stream<ResourceKey<Biome>> getBiomeReplacements(ResourceKey<Biome> target) {
        if (biomeReplacements.containsKey(target)) {
            return biomeReplacements.get(target).stream();
        } else {
            return Stream.empty();
        }
    }

    public void addBiomeReplacements(ResourceKey<Biome> target, Stream<ResourceKey<Biome>> replacements) {
        if (biomeReplacements.containsKey(target)) {
            replacements.forEachOrdered(biomeReplacements.get(target)::add);
        } else {
            biomeReplacements.put(target, replacements.collect(Collectors.toCollection(LinkedHashSet::new)));
        }

        this.setDirty();
    }

    public Identifier getDimensionId() {
        if (world.dimensionTypeRegistration().unwrapKey().isEmpty()) {
            return Identifier.fromNamespaceAndPath("biolith", "unregistered_dimension");
        }

        return world.dimensionTypeRegistration().unwrapKey().get().identifier();
    }

    public Identifier getWorldId() {
        return world.dimension().identifier();
    }
}
