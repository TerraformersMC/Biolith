package com.terraformersmc.biolith.impl.platform.services;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompat;
import org.apache.commons.lang3.function.TriFunction;

import java.nio.file.Path;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public interface PlatformHelper {
    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * Gets the filesystem {@link Path} of the game config storage.
     *
     * @return The path of the platform's instance configuration directory.
     */
    Path getConfigDir();

    /**
     * Sends the biolith describe output for Moderner Beta biome sources.
     *
     * @param context Command context
     * @param biomeX Biome X position
     * @param biomeY Biome Y position
     * @param biomeZ Biome Z position
     * @param world ServerWorld containing the position
     * @param biomeSource Relevant biome source
     * @param biomeEntries Biome entries if any
     * @return 0 to pass; otherwise the command return value
     */
    default int describeModernerBeta(CommandContext<CommandSourceStack> context, int biomeX, int biomeY, int biomeZ, ServerLevel world, BiomeSource biomeSource, Climate.ParameterList<Holder<Biome>> biomeEntries, Climate.Sampler noise) {
        return 0;
    }

    /**
     * Gets the current platform's implementation of TerraBlenderCompat.
     *
     * @return The platform's implementation of TerraBlenderCompat.
     */
    TerraBlenderCompat getTerraBlenderCompat();

    /**
     * Registers a command registration callback with the platform's event system.
     *
     * @param callback {@code (dispatcher, registryAccess, environment) -> { dispatcher.register(COMMANDS) } }
     */
    void registerCommandRegistrationCallback(TriFunction<CommandDispatcher<CommandSourceStack>, CommandBuildContext, Commands.CommandSelection, LiteralCommandNode<CommandSourceStack>> callback);
}