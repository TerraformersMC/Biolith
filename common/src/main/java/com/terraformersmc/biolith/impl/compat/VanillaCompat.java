package com.terraformersmc.biolith.impl.compat;

import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.apache.commons.lang3.function.TriFunction;

public class VanillaCompat {
    @SuppressWarnings("unchecked")
    // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
    public static BiolithFittestNodes<Holder<Biome>> getBiome(Climate.TargetPoint noisePoint, Climate.ParameterList<Holder<Biome>> entries) {
        return entries.index.biolith$searchTreeGet(noisePoint, Climate.RTree.Node::distance);
    }

    public static BiolithFittestNodes<Holder<Biome>> getEndBiome(Climate.TargetPoint noisePoint, Climate.ParameterList<Holder<Biome>> entries, Holder<Biome> original) {
        BiolithFittestNodes<Holder<Biome>> fittestNodes;

        if (original.is(Biomes.THE_END)) {
            // We do not use noise to replace the central End biome; replacements must be explicit.
            // As such, there is no second-best-fit biome.
            Climate.RTree.Leaf<Holder<Biome>> ultimate = new Climate.RTree.Leaf<>(
                    new Climate.ParameterPoint(
                            Climate.Parameter.point(Climate.unquantizeCoord(noisePoint.temperature())),
                            Climate.Parameter.point(Climate.unquantizeCoord(noisePoint.humidity())),
                            Climate.Parameter.point(Climate.unquantizeCoord(noisePoint.continentalness())),
                            Climate.Parameter.point(Climate.unquantizeCoord(noisePoint.erosion())),
                            Climate.Parameter.point(Climate.unquantizeCoord(noisePoint.depth())),
                            Climate.Parameter.point(Climate.unquantizeCoord(noisePoint.weirdness())),
                            0L),
                    original);
            fittestNodes = new BiolithFittestNodes<>(ultimate, 0);
        } else {
            // Evaluate the best fit biome by noise at the noise point.
            // Unchecked because of parameterized types (which are always RegistryEntry<Biome>)
            //noinspection unchecked
            fittestNodes = entries.index.biolith$searchTreeGet(noisePoint, Climate.RTree.Node::distance);
        }

        // If the best noise fit was a vanilla biome, let whatever vanilla picked leak through.
        // This way if other mods have directly modified vanilla biome selection, it may still work.
        if (!original.equals(fittestNodes.ultimate().value) && (
                fittestNodes.ultimate().value.is(Biomes.SMALL_END_ISLANDS) ||
                        fittestNodes.ultimate().value.is(Biomes.END_BARRENS) ||
                        fittestNodes.ultimate().value.is(Biomes.END_MIDLANDS) ||
                        fittestNodes.ultimate().value.is(Biomes.END_HIGHLANDS))) {

            fittestNodes = new BiolithFittestNodes<>(
                    new Climate.RTree.Leaf<>(createNoiseHypercube(fittestNodes.ultimate().parameterSpace), original),
                    0L,
                    fittestNodes.ultimate(),
                    fittestNodes.ultimateDistance()
            );
        }

        return fittestNodes;
    }

    // This is a smoothed version of vanilla's End biome placement.
    public static Holder<Biome> getOriginalEndBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler noise) {
        if (BiomeCoordinator.END.nodeEndHighlands == null) {
            throw new UnsupportedOperationException("VanillaCompat.getOriginalEndBiome called before End level created.");
        }
        assert  BiomeCoordinator.END.nodeTheEnd != null &&
                BiomeCoordinator.END.nodeEndMidlands != null &&
                BiomeCoordinator.END.nodeSmallEndIslands != null &&
                BiomeCoordinator.END.nodeEndBarrens != null;

        Holder<Biome> biomeEntry;

        int x = QuartPos.toBlock(biomeX);
        int y = QuartPos.toBlock(biomeY);
        int z = QuartPos.toBlock(biomeZ);

        if (Mth.square((long) SectionPos.blockToSectionCoord(x)) +
                Mth.square((long) SectionPos.blockToSectionCoord(z)) <= 4096L) {
            biomeEntry = BiomeCoordinator.END.nodeTheEnd.value;
        } else {
            double erosion = noise.erosion().compute(new DensityFunction.SinglePointContext(x, y, z));
            if (erosion > 0.25) {
                biomeEntry = BiomeCoordinator.END.nodeEndHighlands.value;
            } else if (erosion >= -0.0625) {
                biomeEntry = BiomeCoordinator.END.nodeEndMidlands.value;
            } else if (erosion < -0.21875) {
                biomeEntry = BiomeCoordinator.END.nodeSmallEndIslands.value;
            } else {
                biomeEntry = BiomeCoordinator.END.nodeEndBarrens.value;
            }
        }

        return biomeEntry;
    }

    private static Climate.ParameterPoint createNoiseHypercube(Climate.Parameter... parameters) {
        return Climate.parameters(parameters[0], parameters[1], parameters[2], parameters[3], parameters[4], parameters[5], parameters[6].min());
    }

    /**
     * This method duplicates the processing of {@link BiomeManager#getBiome} which smooths out the 4x4x4
     * biome pixels using the world seed as a source of "randomness".  It then calls the provided getBiome
     * function and returns whatever the function does.  Thus, use of other getBiome implementations is
     * possible without reimplementing chunk storage.
     *
     * @param function A getBiome tri-function mapping biome coordinates (x, y, z) to Holder of Biome
     * @param pos The BlockPos to target with function
     * @param seed The seed of the relevant world
     * @return Holder of Biome returned for the smoothed biome coordinates
     */
    public static Holder<Biome> callFunctionWithSmoothedBiomeCoords(TriFunction<Integer, Integer, Integer, Holder<Biome>> function, BlockPos pos, long seed) {
        int centerX = pos.getX() - 2;
        int centerY = pos.getY() - 2;
        int centerZ = pos.getZ() - 2;
        int biomeX = centerX >> 2;
        int biomeY = centerY >> 2;
        int biomeZ = centerZ >> 2;
        double fractionalX = (double) (centerX & 3) / 4.0;
        double fractionalY = (double) (centerY & 3) / 4.0;
        double fractionalZ = (double) (centerZ & 3) / 4.0;

        int offsets = 0;
        double minimum = Double.POSITIVE_INFINITY;
        for (int option = 0; option < 8; ++option) {
            boolean offsetX = (option & 4) == 0;
            boolean offsetY = (option & 2) == 0;
            boolean offsetZ = (option & 1) == 0;
            double preference = BiomeManager.getFiddledDistance(
                    seed,
                    offsetX ? biomeX : biomeX + 1,
                    offsetY ? biomeY : biomeY + 1,
                    offsetZ ? biomeZ : biomeZ + 1,
                    offsetX ? fractionalX : fractionalX - 1.0,
                    offsetY ? fractionalY : fractionalY - 1.0,
                    offsetZ ? fractionalZ : fractionalZ - 1.0);
            if (minimum > preference) {
                minimum = preference;
                offsets = option;
            }
        }

        return function.apply(
                (offsets & 4) == 0 ? biomeX : biomeX + 1,
                (offsets & 2) == 0 ? biomeY : biomeY + 1,
                (offsets & 1) == 0 ? biomeZ : biomeZ + 1);
    }
}
