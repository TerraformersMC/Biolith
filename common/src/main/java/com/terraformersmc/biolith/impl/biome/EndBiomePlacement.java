package com.terraformersmc.biolith.impl.biome;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.config.BiolithState;
import com.terraformersmc.biolith.impl.noise.OpenSimplexNoise2;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class EndBiomePlacement extends DimensionBiomePlacement {
    private final double[] scale = new double[4];

    public Climate.RTree.@Nullable Leaf<Holder<Biome>> nodeTheEnd;
    public Climate.RTree.@Nullable Leaf<Holder<Biome>> nodeSmallEndIslands;
    public Climate.RTree.@Nullable Leaf<Holder<Biome>> nodeEndBarrens;
    public Climate.RTree.@Nullable Leaf<Holder<Biome>> nodeEndMidlands;
    public Climate.RTree.@Nullable Leaf<Holder<Biome>> nodeEndHighlands;

    private @Nullable OpenSimplexNoise2 humidityNoise;
    private @Nullable OpenSimplexNoise2 temperatureNoise;
    private @Nullable OpenSimplexNoise2 weirdnessNoise;

    public EndBiomePlacement() {
        super();

        int configScale = Biolith.getConfigManager().getGeneralConfig().getEndReplacementScale();
        scale[0] = 256 * configScale;
        scale[1] =  64 * configScale;
        scale[2] =  16 * configScale;
        scale[3] =   4 * configScale;
    }

    @Override
    protected void serverReplaced(BiolithState state, ServerLevel world) {
        super.serverReplaced(state, world);

        // Update vanilla biome entries for the End
        HolderGetter<Biome> biomeEntryGetter = BiomeCoordinator.getBiomeLookupOrThrow();
        nodeTheEnd          = new Climate.RTree.Leaf<>(OUT_OF_RANGE,                                      biomeEntryGetter.getOrThrow(Biomes.THE_END));
        nodeSmallEndIslands = new Climate.RTree.Leaf<>(VanillaEndBiomeParameters.NOISE_SMALL_END_ISLANDS, biomeEntryGetter.getOrThrow(Biomes.SMALL_END_ISLANDS));
        nodeEndBarrens      = new Climate.RTree.Leaf<>(VanillaEndBiomeParameters.NOISE_END_BARRENS,       biomeEntryGetter.getOrThrow(Biomes.END_BARRENS));
        nodeEndMidlands     = new Climate.RTree.Leaf<>(VanillaEndBiomeParameters.NOISE_END_MIDLANDS,      biomeEntryGetter.getOrThrow(Biomes.END_MIDLANDS));
        nodeEndHighlands    = new Climate.RTree.Leaf<>(VanillaEndBiomeParameters.NOISE_END_HIGHLANDS,     biomeEntryGetter.getOrThrow(Biomes.END_HIGHLANDS));

        // Seed the End simplex noises based on the game seed
        humidityNoise    = new OpenSimplexNoise2(seedlets[7]);
        temperatureNoise = new OpenSimplexNoise2(seedlets[5]);
        weirdnessNoise   = new OpenSimplexNoise2(seedlets[3]);
    }

    @Override
    protected void serverStopped() {
        super.serverStopped();

        nodeTheEnd = null;
        nodeSmallEndIslands = null;
        nodeEndBarrens = null;
        nodeEndMidlands = null;
        nodeEndHighlands = null;
    }

    @Override
    public double getLocalNoise(int x, int y, int z) {
        Objects.requireNonNull(replacementNoise);

        double localNoise;

        // Four octaves to give some edge fuzz
        localNoise  = replacementNoise.sample((double)(x + seedlets[0]) / scale[0], (double)(z + seedlets[1]) / scale[0]);
        localNoise += replacementNoise.sample((double)(x + seedlets[2]) / scale[1], (double)(z + seedlets[3]) / scale[1]) / 4D;
        localNoise += replacementNoise.sample((double)(x + seedlets[4]) / scale[2], (double)(z + seedlets[5]) / scale[2]) / 16D;
        localNoise += replacementNoise.sample((double)(x + seedlets[6]) / scale[3], (double)(z + seedlets[7]) / scale[3]) / 32D;

        // Scale the result back to amplitude 1 and then normalize
        localNoise = normalize(localNoise / 1.3125D);

        return localNoise;
    }

    // TODO: Should use DimensionBiomePlacement method instead,
    //       but availability of biomeEntryGetter must be thoroughly validated first.
    public void writeBiomeParameters(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> parameters) {
        biomesInjected = true;

        // End biomes are merged during construction of the End Biome stream.

        placementRequests.forEach(request -> parameters.accept(request.pair()));

        // Replacement biomes are placed out-of-range so they do not generate except as replacements.
        // This adds the biome to TheEndBiomeSource and BiomeSource so features and structures will place.

        replacementRequests.values().stream()
                .flatMap(requestSet -> requestSet.requests.stream())
                .map(ReplacementRequest::biome).distinct()
                .forEach(biome -> {
                    if (!biome.equals(VANILLA_PLACEHOLDER)) {
                        parameters.accept(Pair.of(OUT_OF_RANGE, biome));
                    }
                });

        subBiomeRequests.values().stream()
                .flatMap(requestSet -> requestSet.requests.stream())
                .map(SubBiomeRequest::biome).distinct()
                .forEach(biome -> parameters.accept(Pair.of(OUT_OF_RANGE, biome)));
    }

    // TODO: This should be replaced with a more robust noise implementation, perhaps also more similar to vanilla.
    public Climate.TargetPoint sampleEndNoise(int x, int y, int z, Climate.Sampler originalNoise, Holder<Biome> originalBiome) {
        Objects.requireNonNull(humidityNoise);
        Objects.requireNonNull(temperatureNoise);
        Objects.requireNonNull(weirdnessNoise);

        double erosion = originalNoise.erosion().compute(new DensityFunction.SinglePointContext(
                QuartPos.toBlock(x),
                QuartPos.toBlock(y),
                QuartPos.toBlock(z)
        ));

        return new Climate.TargetPoint(
                Climate.quantizeCoord(temperatureNoise.sample(x / 576d, z / 576d)),
                Climate.quantizeCoord(humidityNoise.sample(x / 448d, z / 448d)),
                originalBiome.is(Biomes.THE_END) ? 0L : Climate.quantizeCoord(Mth.clamp((float) erosion + 0.0625f, -1f, 1f)),
                Climate.quantizeCoord((float) erosion),
                156L * (56 - y),
                Climate.quantizeCoord(weirdnessNoise.sample(x / 192d, z / 192d))
        );
    }
}
