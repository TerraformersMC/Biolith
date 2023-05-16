package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.compat.InterfaceEntries;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import terrablender.worldgen.noise.Area;

/**
 * This mixin provides access to TerraBlender private variables in the noise entries class so Biolith can
 * provide seamless integration for TerraBlender worlds (using Biolith's tree search in TerraBlender regions).
 */
@SuppressWarnings("unused")
@Mixin(MultiNoiseUtil.Entries.class)
public class MixinEntries<T> implements InterfaceEntries<T> {
    private boolean initialized;
    private boolean treesPopulated;
    private Area uniqueness;
    private MultiNoiseUtil.SearchTree<T>[] uniqueTrees;

    @Override
    public boolean biolith$getInitialized() {
        return initialized;
    }

    @Override
    public boolean biolith$getTreesPopulated() {
        return treesPopulated;
    }

    @Override
    public Object biolith$getUniqueness() {
        return uniqueness;
    }

    @Override
    public MultiNoiseUtil.SearchTree<T>[] biolith$getUniqueTrees() {
        return uniqueTrees;
    }

    @Override
    @Nullable
    public MultiNoiseUtil.SearchTree<T> biolith$getuniqueTree(int x, int y, int z) {
        return (initialized && treesPopulated) ? this.uniqueTrees[this.uniqueness.get(x, z)] : null;
    }
}
