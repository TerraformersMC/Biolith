package com.terraformersmc.biolith.impl.platform.services;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompat;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.apache.commons.lang3.function.TriFunction;

import java.nio.file.Path;

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
     * Sends the biolith describe output for Modern Beta biome sources.
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
    default int describeModernBeta(CommandContext<ServerCommandSource> context, int biomeX, int biomeY, int biomeZ, ServerWorld world, BiomeSource biomeSource, MultiNoiseUtil.Entries<RegistryEntry<Biome>> biomeEntries, MultiNoiseUtil.MultiNoiseSampler noise) {
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
    void registerCommandRegistrationCallback(TriFunction<CommandDispatcher<ServerCommandSource>, CommandRegistryAccess, CommandManager.RegistrationEnvironment, LiteralCommandNode<ServerCommandSource>> callback);
}