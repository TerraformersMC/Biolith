
package com.terraformersmc.biolith.impl.platform;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.terraformersmc.biolith.impl.compat.ModernBetaCompatFabric;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompat;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompatFabric;
import com.terraformersmc.biolith.impl.platform.services.PlatformHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
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

public class FabricPlatformHelper implements PlatformHelper {
    private static final TerraBlenderCompat COMPAT_TERRABLENDER = new TerraBlenderCompatFabric();

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public int describeModernBeta(CommandContext<ServerCommandSource> context, int biomeX, int biomeY, int biomeZ, ServerWorld world, BiomeSource biomeSource, MultiNoiseUtil.Entries<RegistryEntry<Biome>> biomeEntries, MultiNoiseUtil.MultiNoiseSampler noise) {
        return ModernBetaCompatFabric.describe(context, biomeX, biomeY, biomeZ, world, biomeSource, biomeEntries, noise);
    }

    @Override
    public TerraBlenderCompat getTerraBlenderCompat() {
        return COMPAT_TERRABLENDER;
    }

    @Override
    public void registerCommandRegistrationCallback(TriFunction<CommandDispatcher<ServerCommandSource>, CommandRegistryAccess, CommandManager.RegistrationEnvironment, LiteralCommandNode<ServerCommandSource>> callback) {
        CommandRegistrationCallback.EVENT.register(callback::apply);
    }
}
