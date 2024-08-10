package com.terraformersmc.biolith.impl.config;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.biome.Biome;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class BiolithState extends PersistentState {
    private final LinkedHashMap<RegistryKey<Biome>, LinkedHashSet<RegistryKey<Biome>>> biomeReplacements = new LinkedHashMap<>(64);
    private final ServerWorld world;

    private final String stateId;
    private static final int STATE_VERSION = 0;

    public BiolithState(ServerWorld serverWorld, String name) {
        // Make sure we've got the server world stowed for state loads/saves.
        world = serverWorld;
        stateId = Biolith.MOD_ID + "_" + name + "_state";
        world.getPersistentStateManager().set(stateId, this);
        this.readState();
    }

    private void writeState() {
        this.markDirty();
        world.getPersistentStateManager().save();
    }

    private void readState() {
        NbtCompound nbt = null;
        NbtCompound nbtState = null;

        try {
            nbt = world.getPersistentStateManager().readNbt(stateId, DataFixTypes.LEVEL, STATE_VERSION);
        } catch (IOException e) {
            Biolith.LOGGER.debug("No saved state found for {}; starting anew...", stateId);
        }
        if (nbt != null && nbt.contains("data")) {
            int nbtVersion = nbt.getInt("DataVersion");
            nbtState = nbt.getCompound("data");
        }

        biomeReplacements.clear();
        if (nbtState != null && !nbtState.isEmpty()) {
            NbtList biomeReplacementsNbt = nbtState.getList("BiomeReplacementsList", NbtList.LIST_TYPE);
            biomeReplacementsNbt.forEach(nbtElement -> {
                NbtList replacementsNbt = (NbtList) nbtElement.copy();
                Identifier elementId = Identifier.tryParse(replacementsNbt.getString(0));
                if (elementId == null) {
                    Biolith.LOGGER.warn("{}: Failed to parse target biome identifier from NBT: {}", stateId, replacementsNbt.getString(0));
                } else if (replacementsNbt.size() < 2) {
                    Biolith.LOGGER.warn("{}: Replacements list from NBT contains no replacements: {}", stateId, replacementsNbt.getString(0));
                } else {
                    RegistryKey<Biome> target = RegistryKey.of(RegistryKeys.BIOME, elementId);
                    replacementsNbt.remove(0);
                    biomeReplacements.put(target, replacementsNbt.stream()
                            .map(element -> Identifier.tryParse(element.asString())).filter(Objects::nonNull)
                            .map(id -> RegistryKey.of(RegistryKeys.BIOME, id))
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                    Biolith.LOGGER.debug("{}: Resolved replacements list from NBT: {} -> {}", stateId, target.getValue(), biomeReplacements.get(target).stream().map(RegistryKey::getValue).toList());
                }
            });
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList biomeReplacementsNbt = new NbtList();
        biomeReplacements.forEach((target, replacements) -> {
            NbtList replacementsNbt = new NbtList();
            replacementsNbt.add(NbtString.of(target.getValue().toString()));
            replacementsNbt.addAll(replacements.stream().map(replacement -> NbtString.of(replacement.getValue().toString())).toList());
            biomeReplacementsNbt.add(replacementsNbt);
        });
        Biolith.LOGGER.debug("{}: Describing biome replacemnts NBT:\n{}", stateId, biomeReplacementsNbt);
        nbt.put("BiomeReplacementsList", biomeReplacementsNbt);

        return nbt;
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
