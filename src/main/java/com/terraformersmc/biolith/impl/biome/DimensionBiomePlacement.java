package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.api.biome.SubBiomeMatcher;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.terraform.noise.OpenSimplexNoise2;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import java.util.*;
import java.util.function.Consumer;

public abstract class DimensionBiomePlacement {
    protected boolean biomesInjected = false;
    protected Registry<Biome> biomeRegistry;
    protected BiolithState state;
    protected OpenSimplexNoise2 replacementNoise;
    protected int[] seedlets = new int[8];
    protected Random seedRandom;
    protected final Collection<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> placementRequests = new HashSet<>(256);
    protected final HashMap<RegistryKey<Biome>, ReplacementRequestSet> replacementRequests = new HashMap<>(256);
    protected final HashMap<RegistryKey<Biome>, SubBiomeRequestSet> subBiomeRequests = new HashMap<>(256);

    public static final MultiNoiseUtil.ParameterRange DEFAULT_PARAMETER = MultiNoiseUtil.ParameterRange.of(-1.0f, 1.0f);
    public static final MultiNoiseUtil.NoiseHypercube OUT_OF_RANGE = MultiNoiseUtil.createNoiseHypercube(3.01f, 3.01f, 3.01f, 3.01f, 3.01f, 3.01f, 3.01f);

    public static final RegistryKey<Biome> VANILLA_PLACEHOLDER = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(Biolith.MOD_ID, "vanilla"));

    protected void serverReplaced(BiolithState state, long seed) {
        DynamicRegistryManager.Immutable registryManager = BiomeCoordinator.getRegistryManager();
        if (registryManager == null) {
            throw new IllegalStateException("Registry manager is null during biome replacement setup!");
        } else {
            biomeRegistry = registryManager.get(RegistryKeys.BIOME);
        }
        this.state = state;
        replacementNoise = new OpenSimplexNoise2(seed);
        seedRandom = new Random(seed);
        replacementRequests.forEach((biomeKey, requestSet) -> requestSet.complete(biomeRegistry));
        subBiomeRequests.forEach((biomeKey, requestSet) -> requestSet.complete(biomeRegistry));

        seedlets[0] = (int) (seed       & 0xffL);
        seedlets[1] = (int) (seed >>  8 & 0xffL);
        seedlets[2] = (int) (seed >> 16 & 0xffL);
        seedlets[3] = (int) (seed >> 24 & 0xffL);
        seedlets[4] = (int) (seed >> 32 & 0xffL);
        seedlets[5] = (int) (seed >> 40 & 0xffL);
        seedlets[6] = (int) (seed >> 48 & 0xffL);
        seedlets[7] = (int) (seed >> 56 & 0xffL);
    }

    public void addPlacement(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube noisePoint) {
        if (biomesInjected) {
            Biolith.LOGGER.error("Biolith's BiomePlacement.addPlacement() called too late for biome: {}", biome.getValue());
        } else {
            placementRequests.add(Pair.of(noisePoint, biome));
        }
    }

    public void addReplacement(RegistryKey<Biome> target, RegistryKey<Biome> biome, double rate) {
        if (biomesInjected) {
            Biolith.LOGGER.error("Biolith's BiomePlacement.addReplacement() called too late for biome: {}", biome.getValue());
        } else {
            replacementRequests.computeIfAbsent(target, ReplacementRequestSet::new).addRequest(biome, rate);
        }
    }

    public void addSubBiome(RegistryKey<Biome> target, RegistryKey<Biome> biome, SubBiomeMatcher matcher) {
        if (biomesInjected) {
            Biolith.LOGGER.error("Biolith's BiomePlacement.addSubBiome() called too late for biome: {}", biome.getValue());
        } else {
            subBiomeRequests.computeIfAbsent(target, SubBiomeRequestSet::new).addRequest(biome, matcher);
        }
    }


    public abstract RegistryEntry<Biome> getReplacement(int x, int y, int z, MultiNoiseUtil.NoiseValuePoint noisePoint, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes);

    public abstract void writeBiomeParameters(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>>> parameters);

    // Approximation of normalizing K.jpg OpenSimplex2(F) values in [-1,1] to unbiased values in [0,1].
    // It's pretty close but values dip a bit near the edges and 1% at the +1 edge is a bit high.
    protected double normalize(double value) {
        return MathHelper.clamp(value * 0.5375D + 0.5D, 0D, 1D);
    }

    protected record ReplacementRequest(RegistryKey<Biome> biome, double rate, RegistryEntry<Biome> biomeEntry, double scaled) {
        public ReplacementRequest {
            rate = MathHelper.clamp(rate, 0D, 1D);
        }

        static ReplacementRequest of(RegistryKey<Biome> biome, double rate) {
            return new ReplacementRequest(biome, rate, null, rate);
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ReplacementRequest request) {
                return request.biome.equals(this.biome) && request.rate == this.rate;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return biome.hashCode();
        }

        ReplacementRequest complete(Registry<Biome> biomeRegistry, double scaled) {
            // Requests must be re-completed after every server restart in case the biome registry has changed.
            // But don't mess with the place-holder; it's always complete and re-completing it will crash.
            if (this.biome.equals(VANILLA_PLACEHOLDER)) {
                return this;
            } else {
                return new ReplacementRequest(biome, rate, biomeRegistry.getEntry(biomeRegistry.getOrThrow(biome)), scaled);
            }
        }
    }

    protected class ReplacementRequestSet {
        RegistryKey<Biome> target;
        List<ReplacementRequest> requests = new ArrayList<>(8);

        ReplacementRequestSet(RegistryKey<Biome> target) {
            this.target = target;
        }

        void addRequest(RegistryKey<Biome> biome, double rate) {
            addRequest(ReplacementRequest.of(biome, rate));
        }

        void addRequest(ReplacementRequest request) {
            if (requests.contains(request)) {
                Biolith.LOGGER.info("Ignoring request for duplicate biome replacement: {}", request.biome);
            } else {
                requests.add(request);
            }
        }

        void complete(Registry<Biome> biomeRegistry) {
            double maxRate = 0D;
            double total = 0D;
            double vanilla;
            double scale;

            // Re-open the list for modification.
            requests = new ArrayList<>(requests);

            // If an integrated server restarts, we need to clear out the previous vanilla place-holder.
            requests.removeIf(request -> request.biome.equals(VANILLA_PLACEHOLDER));

            // Calculate biome distribution scale.
            for (ReplacementRequest request : requests) {
                total += request.rate;
                if (request.rate > maxRate) {
                    maxRate = request.rate;
                }
            }
            vanilla = MathHelper.clamp(1D - maxRate, 0D, 1D);
            scale = total + vanilla;

            // Add a special request with a place-holder for the vanilla biome, if/when it still generates.
            if (vanilla > 0D) {
                requests.add(new ReplacementRequest(VANILLA_PLACEHOLDER, vanilla, null, vanilla / scale));
            }

            // Update saved state with any additions and fetch the new order.
            Collections.shuffle(requests, seedRandom);
            state.addBiomeReplacements(target, requests.stream().map(ReplacementRequest::biome));
            List<RegistryKey<Biome>> sortOrder = state.getBiomeReplacements(target).toList();

            // Finalize the request list and store it in state order.
            requests = requests.stream()
                    .map(request -> request.complete(biomeRegistry, request.rate / scale))
                    .sorted(Comparator.comparingInt(request -> sortOrder.indexOf(request.biome)))
                    .toList();
        }
    }

    protected record SubBiomeRequest(RegistryKey<Biome> biome, SubBiomeMatcher matcher, RegistryEntry<Biome> biomeEntry) {
        static SubBiomeRequest of(RegistryKey<Biome> biome, SubBiomeMatcher matcher) {
            return new SubBiomeRequest(biome, matcher, null);
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof SubBiomeRequest request) {
                return request.biome.equals(this.biome) && request.matcher.equals(this.matcher);
            }

            return false;
        }

        @Override
        public int hashCode() {
            // TODO: plus somehow consider the validity ranges?
            return biome.hashCode();
        }

        SubBiomeRequest complete(Registry<Biome> biomeRegistry) {
            // Requests must be re-completed after every server restart in case the biome registry has changed.
            return new SubBiomeRequest(biome, matcher, biomeRegistry.getEntry(biomeRegistry.getOrThrow(biome)));
        }
    }

    protected class SubBiomeRequestSet {
        RegistryKey<Biome> target;
        List<SubBiomeRequest> requests = new ArrayList<>(8);

        SubBiomeRequestSet(RegistryKey<Biome> target) {
            this.target = target;
        }

        void addRequest(RegistryKey<Biome> biome, SubBiomeMatcher matcher) {
            addRequest(SubBiomeRequest.of(biome, matcher));
        }

        void addRequest(SubBiomeRequest request) {
            if (requests.contains(request)) {
                Biolith.LOGGER.info("Ignoring request for duplicate sub-biome: {} -> {}", target, request.biome);
            } else {
                requests.add(request);
            }
        }

        void complete(Registry<Biome> biomeRegistry) {
            // Re-open the list for modification.
            requests = new ArrayList<>(requests);

            // Finalize the request list and store it in a somewhat stable order.
            requests = requests.stream()
                    .map(request -> request.complete(biomeRegistry))
                    .sorted(Comparator.comparing(request -> request.biome.getValue()))
                    .toList();
        }
    }
}