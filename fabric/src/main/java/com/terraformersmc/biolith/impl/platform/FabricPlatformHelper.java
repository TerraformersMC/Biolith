package com.terraformersmc.biolith.impl.platform;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.compat.ModernerBetaCompatFabric;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompat;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompatFabric;
import com.terraformersmc.biolith.impl.platform.services.PlatformHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.SurfaceRules;
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

    @Override
    public int describeModernerBeta(CommandContext<CommandSourceStack> context, int biomeX, int biomeY, int biomeZ, ServerLevel world, BiomeSource biomeSource, Climate.ParameterList<Holder<Biome>> biomeEntries, Climate.Sampler noise) {
        return ModernerBetaCompatFabric.describe(context, biomeX, biomeY, biomeZ, world, biomeSource, biomeEntries, noise);
    }

    @Override
    public TerraBlenderCompat getTerraBlenderCompat() {
        return COMPAT_TERRABLENDER;
    }

    @Override
    public void registerCommandRegistrationCallback(TriFunction<CommandDispatcher<CommandSourceStack>, CommandBuildContext, Commands.CommandSelection, LiteralCommandNode<CommandSourceStack>> callback) {
        CommandRegistrationCallback.EVENT.register(callback::apply);
    }

    @Override
    public void registerMaterialCondition(String name, MapCodec<? extends SurfaceRules.ConditionSource> codec) {
        Registry.register(BuiltInRegistries.MATERIAL_CONDITION, Biolith.key(BuiltInRegistries.MATERIAL_CONDITION.key(), name), codec);
    }
}
