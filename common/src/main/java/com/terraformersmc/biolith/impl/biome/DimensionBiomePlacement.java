package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.api.biome.SubBiomeMatcher;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.commands.BiolithDescribeCommand;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.biolith.impl.noise.OpenSimplexNoise2;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import java.util.*;
import java.util.function.Consumer;

public abstract class DimensionBiomePlacement {
    protected boolean biomesInjected = false;
    protected BiolithState state;
    protected OpenSimplexNoise2 replacementNoise;
    protected int[] seedlets = new int[8];
    protected Random seedRandom;
    protected final Collection<PlacementRequest> placementRequests = new HashSet<>(256);
    protected final Collection<RemovalRequest> removalRequests = new HashSet<>(256);
    protected final HashMap<RegistryKey<Biome>, ReplacementRequestSet> replacementRequests = new HashMap<>(256);
    protected final HashMap<RegistryKey<Biome>, SubBiomeRequestSet> subBiomeRequests = new HashMap<>(256);

    public static final MultiNoiseUtil.ParameterRange DEFAULT_PARAMETER = MultiNoiseUtil.ParameterRange.of(-1.0f, 1.0f);
    public static final MultiNoiseUtil.NoiseHypercube OUT_OF_RANGE = MultiNoiseUtil.createNoiseHypercube(3.01f, 3.01f, 3.01f, 3.01f, 3.01f, 3.01f, 3.01f);

    public static final RegistryKey<Biome> VANILLA_PLACEHOLDER = RegistryKey.of(RegistryKeys.BIOME, Identifier.of(Biolith.MOD_ID, "vanilla"));

    protected void serverReplaced(BiolithState state, long seed) {
        this.state = state;
        this.replacementNoise = new OpenSimplexNoise2(seed);
        this.seedRandom = new Random(seed);
        this.replacementRequests.forEach((biomeKey, requestSet) -> requestSet.complete(BiomeCoordinator.getBiomeLookupOrThrow()));
        this.subBiomeRequests.forEach((biomeKey, requestSet) -> requestSet.complete(BiomeCoordinator.getBiomeLookupOrThrow()));

        seedlets[0] = (int) (seed       & 0xffL);
        seedlets[1] = (int) (seed >>  8 & 0xffL);
        seedlets[2] = (int) (seed >> 16 & 0xffL);
        seedlets[3] = (int) (seed >> 24 & 0xffL);
        seedlets[4] = (int) (seed >> 32 & 0xffL);
        seedlets[5] = (int) (seed >> 40 & 0xffL);
        seedlets[6] = (int) (seed >> 48 & 0xffL);
        seedlets[7] = (int) (seed >> 56 & 0xffL);
    }

    protected void serverStopped() {
        biomesInjected = false;
        state = null;

        // Reopen completed request lists.
        replacementRequests.forEach((key, list) -> list.reopen());
        subBiomeRequests.forEach((key, list) -> list.reopen());
    }


    public void addPlacement(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube noisePoint, boolean fromData) {
        if (biomesInjected) {
            Biolith.LOGGER.error("Biolith's BiomePlacement.addPlacement() called too late for biome: {}", biome.getValue());
        } else {
            placementRequests.add(new PlacementRequest(noisePoint, biome, fromData));
        }
    }

    public void addRemoval(RegistryKey<Biome> biome, boolean fromData) {
        if (biomesInjected) {
            Biolith.LOGGER.error("Biolith's BiomePlacement.addRemoval() called too late for biome: {}", biome.getValue());
        } else {
            removalRequests.add(new RemovalRequest(biome, fromData));
        }
    }

    public void addReplacement(RegistryKey<Biome> target, RegistryKey<Biome> biome, double rate, boolean fromData) {
        if (biomesInjected || (replacementRequests.containsKey(target) && replacementRequests.get(target).finalized)) {
            Biolith.LOGGER.error("Biolith's BiomePlacement.addReplacement() called too late for biome: {}", biome.getValue());
        } else {
            replacementRequests.computeIfAbsent(target, ReplacementRequestSet::new).addRequest(biome, rate, fromData);
        }
    }

    public void addSubBiome(RegistryKey<Biome> target, RegistryKey<Biome> biome, SubBiomeMatcher matcher, boolean fromData) {
        if (biomesInjected || (subBiomeRequests.containsKey(target) && subBiomeRequests.get(target).finalized)) {
            Biolith.LOGGER.error("Biolith's BiomePlacement.addSubBiome() called too late for biome: {}", biome.getValue());
        } else {
            subBiomeRequests.computeIfAbsent(target, SubBiomeRequestSet::new).addRequest(biome, matcher, fromData);
        }
    }

    public void clearFromData() {
        placementRequests.removeIf(PlacementRequest::fromData);
        removalRequests.removeIf(RemovalRequest::fromData);
        replacementRequests.forEach((key, set) -> set.requests.removeIf(ReplacementRequest::fromData));
        subBiomeRequests.forEach((key, set) -> set.requests.removeIf(SubBiomeRequest::fromData));
    }

    public RegistryEntry<Biome> getReplacement(int x, int y, int z, MultiNoiseUtil.NoiseValuePoint noisePoint, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes) {
        RegistryEntry<Biome> biomeEntry = fittestNodes.ultimate().value;
        RegistryKey<Biome> biomeKey = biomeEntry.getKey().orElseThrow();

        double localNoise = -1D;
        Vector2f localRange = null;

        // select phase one -- direct replacements
        if (replacementRequests.containsKey(biomeKey)) {
            localNoise = getLocalNoise(x, y, z);
            ReplacementRequest request = replacementRequests.get(biomeKey).selectReplacement(localNoise);

            if (request != null) {
                localRange = request.range();

                if (!request.biome().equals(VANILLA_PLACEHOLDER)) {
                    biomeEntry = request.biomeEntry();
                    biomeKey = request.biome();
                }
            }
        }

        // select phase two -- sub-biome replacements
        if (subBiomeRequests.containsKey(biomeKey)) {
            if (localNoise < 0D) {
                localNoise = getLocalNoise(x, y, z);
            }
            SubBiomeRequest request = subBiomeRequests.get(biomeKey).selectSubBiome(fittestNodes, noisePoint, localRange, localNoise);

            if (request != null) {
                biomeEntry = request.biomeEntry();
                biomeKey = request.biome();
            }
        }

        return biomeEntry;
    }

    /*
     * This function is used by BiolithDescribeCommand to peek under the hood of getReplacement();
     * it is not performance-sensitive like the above function which it imitates.
     */
    public BiolithDescribeCommand.DescribeBiomeData getBiomeData(int x, int y, int z, MultiNoiseUtil.NoiseValuePoint noisePoint, BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes) {
        RegistryEntry<Biome> biomeEntry = fittestNodes.ultimate().value;
        RegistryKey<Biome> biomeKey = biomeEntry.getKey().orElseThrow();
        double localNoise = getLocalNoise(x, y, z);

        ReplacementRequest lowerRequest = null;
        ReplacementRequest replacementRequest = null;
        ReplacementRequest higherRequest = null;
        SubBiomeRequest subBiomeRequest = null;

        if (replacementRequests.containsKey(biomeKey)) {
            for (ReplacementRequest request : replacementRequests.get(biomeKey).requests) {
                if (request.end > localNoise) {
                    if (replacementRequest == null) {
                        replacementRequest = request;
                    } else {
                        higherRequest = request;
                        break;
                    }
                } else {
                    lowerRequest = request;
                }
            }
        }

        if (subBiomeRequests.containsKey(biomeKey)) {
            if (replacementRequest == null) {
                subBiomeRequest = subBiomeRequests.get(biomeKey).
                        selectSubBiome(fittestNodes, noisePoint, null, localNoise);
            } else {
                subBiomeRequest = subBiomeRequests.get(replacementRequest.biome()).
                        selectSubBiome(fittestNodes, noisePoint, replacementRequest.range(), localNoise);
            }
        }

        // Don't leak the vanilla placeholder.  Nulls mean there is no data.
        return new BiolithDescribeCommand.DescribeBiomeData(
                replacementRequest == null ? null : replacementRequest.range(),
                replacementRequest == null ? null : replacementRequest.biome().equals(VANILLA_PLACEHOLDER) ?
                        fittestNodes.ultimate().value.getKey().orElseThrow() : replacementRequest.biome(),
                lowerRequest       == null ? null : lowerRequest.biome().equals(VANILLA_PLACEHOLDER) ?
                        fittestNodes.ultimate().value.getKey().orElseThrow() : lowerRequest.biome(),
                higherRequest      == null ? null : higherRequest.biome().equals(VANILLA_PLACEHOLDER) ?
                        fittestNodes.ultimate().value.getKey().orElseThrow() : higherRequest.biome(),
                subBiomeRequest    == null || subBiomeRequest.biome().equals(VANILLA_PLACEHOLDER) ? null :
                        subBiomeRequest.biome());
    }

    // For Modern Beta and any other similar direct-only schemes.
    public RegistryEntry<Biome> getDirectReplacement(int x, int y, int z, RegistryEntry<Biome> biomeEntry) {
        RegistryKey<Biome> biomeKey = biomeEntry.getKey().orElseThrow();

        // select phase one -- direct replacements
        if (replacementRequests.containsKey(biomeKey)) {
            ReplacementRequest request = replacementRequests.get(biomeKey).selectReplacement(getLocalNoise(x, y, z));

            if (request != null) {

                if (!request.biome().equals(VANILLA_PLACEHOLDER)) {
                    biomeEntry = request.biomeEntry();
                }
            }
        }

        return biomeEntry;
    }

    // Used by mixins to add new noise biome placements.
    public void writeBiomeEntries(Consumer<Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>>> parameters) {
        biomesInjected = true;
        RegistryEntryLookup<Biome> biomeEntryGetter = BiomeCoordinator.getBiomeLookupOrThrow();

        // MultiNoise-based biomes are added directly to the parameters list.

        placementRequests.forEach(request -> parameters.accept(request.pair().mapSecond(biomeEntryGetter::getOrThrow)));

        // Replacement biomes are placed out-of-range so they do not generate except as replacements.
        // This adds the biome to MultiNoiseBiomeSource and BiomeSource so features and structures will place.

        replacementRequests.values().stream()
                .flatMap(requestSet -> requestSet.requests.stream())
                .map(ReplacementRequest::biome).distinct()
                .forEach(biome -> {
                    if (!biome.equals(VANILLA_PLACEHOLDER)) {
                        parameters.accept(Pair.of(OUT_OF_RANGE, biomeEntryGetter.getOrThrow(biome)));
                    }
                });

        subBiomeRequests.values().stream()
                .flatMap(requestSet -> requestSet.requests.stream())
                .map(SubBiomeRequest::biome).distinct()
                .forEach(biome -> parameters.accept(Pair.of(OUT_OF_RANGE, biomeEntryGetter.getOrThrow(biome))));
    }

    /**
     * Used by mixins to remove existing vanilla/data noise biome placements.
     *
     * @param entryPair Noise hypercube and biome registry entry of removal request to evaluate
     * @return Whether the biome should be <b>retained</b> (false indicates removal)
     */
    public boolean removalFilter(Pair<MultiNoiseUtil.NoiseHypercube, RegistryEntry<Biome>> entryPair) {
        for (RemovalRequest removalRequest : removalRequests) {
            if (entryPair.getSecond().matchesKey(removalRequest.biome)) {
                return false;
            }
        }

        return true;
    }

    public abstract double getLocalNoise(int x, int y, int z);

    // Approximation of normalizing K.jpg OpenSimplex2(F) values in [-1,1] to unbiased values in [0,1).
    // It's pretty close but values dip a bit near the edges and 1% at the +1 edge is a bit high.
    protected double normalize(double value) {
        return MathHelper.clamp(value * 0.5375D + 0.5D, 0D, 1D - Double.MIN_NORMAL);
    }

    protected record PlacementRequest(MultiNoiseUtil.NoiseHypercube hypercube, RegistryKey<Biome> biome, boolean fromData) {
        public Pair<MultiNoiseUtil.NoiseHypercube, RegistryKey<Biome>> pair() {
            return Pair.of(hypercube, biome);
        }
    }

    protected record RemovalRequest(RegistryKey<Biome> biome, boolean fromData) {
    }

    protected record ReplacementRequest(RegistryKey<Biome> biome, double rate, RegistryEntry<Biome> biomeEntry, double start, double end, boolean fromData) {
        public ReplacementRequest {
            rate = MathHelper.clamp(rate, 0D, 1D);
        }

        static ReplacementRequest of(RegistryKey<Biome> biome, double rate, boolean fromData) {
            return new ReplacementRequest(biome, rate, null, 0D, 0D, fromData);
        }

        // Reduced precision range packaged for sub-biome matchers.
        public Vector2f range() {
            return new Vector2f((float) start, end > 0.9999D ? 1f : (float) end);
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

        ReplacementRequest complete(RegistryEntryLookup<Biome> biomeEntryGetter, double start, double end) {
            // Requests must be re-completed after every server restart in case the biome registry has changed.
            // But don't try to resolve the place-holder; it has no registry entry and will crash.
            if (this.biome.equals(VANILLA_PLACEHOLDER)) {
                return new ReplacementRequest(biome, rate, null, start, end, false);
            } else {
                return new ReplacementRequest(biome, rate, biomeEntryGetter.getOrThrow(biome), start, end, fromData);
            }
        }
    }

    protected class ReplacementRequestSet {
        private boolean finalized = false;
        RegistryKey<Biome> target;
        List<ReplacementRequest> requests = new ArrayList<>(8);

        ReplacementRequestSet(RegistryKey<Biome> target) {
            this.target = target;
        }

        void addRequest(RegistryKey<Biome> biome, double rate, boolean fromData) {
            addRequest(ReplacementRequest.of(biome, rate, fromData));
        }

        void addRequest(ReplacementRequest request) {
            if (requests.contains(request)) {
                Biolith.LOGGER.info("Ignoring request for duplicate biome replacement: {}", request.biome);
            } else {
                requests.add(request);
            }
        }

        public @Nullable ReplacementRequest selectReplacement(double localNoise) {
            for (ReplacementRequest request : requests) {
                if (request.end > localNoise) {
                    return request;
                }
            }

            return null;
        }

        void complete(RegistryEntryLookup<Biome> biomeEntryGetter) {
            double maxRate = 0D;
            double locus;
            double vanilla;
            double scale;

            if (finalized) {
                throw new IllegalStateException("Attempted to finalize replacement requests without first reopening!");
            }

            // If an integrated server restarts, we need to clear out the previous vanilla place-holder.
            requests.removeIf(request -> request.biome.equals(VANILLA_PLACEHOLDER));

            // Calculate biome distribution scale.
            locus = 0D;
            for (ReplacementRequest request : requests) {
                locus += request.rate;
                if (request.rate > maxRate) {
                    maxRate = request.rate;
                }
            }
            vanilla = MathHelper.clamp(1D - maxRate, 0D, 1D);
            scale = locus + vanilla;

            // Add a special request with a place-holder for the vanilla biome, if/when it still generates.
            if (vanilla > 0D) {
                requests.add(ReplacementRequest.of(VANILLA_PLACEHOLDER, vanilla, false));
            }

            // Update saved state with any additions and sort the requests in the new state order.
            Collections.shuffle(requests, seedRandom);
            state.addBiomeReplacements(target, requests.stream().map(ReplacementRequest::biome));
            List<RegistryKey<Biome>> sortOrder = state.getBiomeReplacements(target).toList();
            requests.sort(Comparator.comparingInt(request -> sortOrder.indexOf(request.biome)));

            // Finalize the request list.
            locus = 0D;
            for (int i = 0; i < requests.size(); ++i) {
                ReplacementRequest request = requests.get(i);
                requests.set(i, request.complete(biomeEntryGetter, locus, locus += request.rate / scale));
            }

            // Store the finalized immutable request list.
            requests = List.copyOf(requests);
            finalized = true;
        }

        void reopen() {
            if (BiomeCoordinator.isServerStarted()) {
                throw new IllegalStateException("Attempted to reopen replacement requests while server is running!");
            }

            requests = new ArrayList<>(requests);
            finalized = false;
        }
    }

    protected record SubBiomeRequest(RegistryKey<Biome> biome, SubBiomeMatcher matcher, RegistryEntry<Biome> biomeEntry, boolean fromData) {
        static SubBiomeRequest of(RegistryKey<Biome> biome, SubBiomeMatcher matcher, boolean fromData) {
            return new SubBiomeRequest(biome, matcher, null, fromData);
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

        SubBiomeRequest complete(RegistryEntryLookup<Biome> biomeEntryGetter) {
            // Requests must be re-completed after every server restart in case the biome registry has changed.
            return new SubBiomeRequest(biome, matcher, biomeEntryGetter.getOrThrow(biome), fromData);
        }
    }

    protected class SubBiomeRequestSet {
        private boolean finalized = false;
        RegistryKey<Biome> target;
        List<SubBiomeRequest> requests = new ArrayList<>(8);

        SubBiomeRequestSet(RegistryKey<Biome> target) {
            this.target = target;
        }

        void addRequest(RegistryKey<Biome> biome, SubBiomeMatcher matcher, boolean fromData) {
            addRequest(SubBiomeRequest.of(biome, matcher, fromData));
        }

        void addRequest(SubBiomeRequest request) {
            if (requests.contains(request)) {
                Biolith.LOGGER.info("Ignoring request for duplicate sub-biome: {} -> {}", target, request.biome);
            } else {
                requests.add(request);
            }
        }

        public @Nullable SubBiomeRequest selectSubBiome(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Vector2fc localRange, double localNoise) {
            for (SubBiomeRequest request : requests) {
                if (request.matcher().matches(fittestNodes, DimensionBiomePlacement.this , noisePoint, localRange, (float) localNoise)) {
                    return request;
                }
            }

            return null;
        }

        void complete(RegistryEntryLookup<Biome> biomeEntryGetter) {
            if (finalized) {
                throw new IllegalStateException("Attempted to finalize sub-biome requests without first reopening!");
            }

            // Finalize the request list and store it in a somewhat stable order.
            requests = requests.stream()
                    .map(request -> request.complete(biomeEntryGetter))
                    .sorted(Comparator.comparing(request -> request.biome.getValue()))
                    .toList();
            finalized = true;
        }

        void reopen() {
            if (BiomeCoordinator.isServerStarted()) {
                throw new IllegalStateException("Attempted to reopen sub-biome requests while server is running!");
            }

            requests = new ArrayList<>(requests);
            finalized = false;
        }
    }
}