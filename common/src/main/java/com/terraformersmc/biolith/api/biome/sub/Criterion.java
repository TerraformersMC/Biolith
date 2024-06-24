package com.terraformersmc.biolith.api.biome.sub;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import com.terraformersmc.biolith.impl.biome.sub.AllOfCriterion;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Range;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;

/**
 * A criterion is a condition or set of conditions which must match in order for a sub-biome to replace a biome.
 * Biolith's default set of criteria are available via the {@link CriterionBuilder} class; see its documentation
 * for details on the default criteria.
 */
public interface Criterion {
    Codec<Criterion> CODEC = CriterionType.TYPE_CODEC.dispatch("type", Criterion::getType, CriterionType::getCodec);
    Codec<Criterion> MATCHER_CODEC = Codec.withAlternative(CODEC, CODEC.listOf(), AllOfCriterion::new);

    /**
     * @return The registered {@link CriterionType} of the criterion
     */
    CriterionType<? extends Criterion> getType();

    /**
     * @return The MapCodec providing serialization of the criterion
     */
    MapCodec<? extends Criterion> getCodec();

    /**
     * Evaluates whether a {@link Criterion} matches the provided state values.
     *
     * @param fittestNodes {@link BiolithFittestNodes} as returned by noise biome evaluation
     * @param biomePlacement {@link DimensionBiomePlacement} of the dimension being generated
     * @param noisePoint {@link MultiNoiseUtil.NoiseValuePoint} at the locus under evaluation
     * @param replacementRange Biolith replacement noise range of the selected replacement biome
     * @param replacementNoise Biolith replacement noise value at the locus under evaluation
     * @return True if the criterion matches the provided data, and false if it does not
     */
    boolean matches(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes, DimensionBiomePlacement biomePlacement, MultiNoiseUtil.NoiseValuePoint noisePoint, @Nullable Range<Float> replacementRange, float replacementNoise);

    /**
     * <p>
     * Called when a game is starting and the biome registry is available.  This is when biome
     * registry entries and any one-time computations should be cached to speed up evaluation of
     * the {@link #matches} method.
     * </p><p>
     * Container Criterion implementations must delegate this method to their contained criteria.
     * </p>
     *
     * @param biomeEntryGetter Biome registry entry lookup for the game that is starting
     */
    default void complete(RegistryEntryLookup<Biome> biomeEntryGetter) {
    }

    /**
     * <p>
     * Called when a game is stopping and cached values such as biome registry entries should be
     * invalidated because they will require re-computation before use.
     * </p><p>
     * Container Criterion implementations must delegate this method to their contained criteria.
     * </p>
     */
    default void reopen() {
    }
}
