package com.terraformersmc.biolith.impl.platform;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.MapCodec;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.compat.ModernerBetaCompatNeoForge;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompat;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompatNeoForge;
import com.terraformersmc.biolith.impl.platform.services.PlatformHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.commons.lang3.function.TriFunction;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements PlatformHelper {
    private static final TerraBlenderCompat COMPAT_TERRABLENDER = new TerraBlenderCompatNeoForge();

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public int describeModernerBeta(CommandContext<CommandSourceStack> context, int biomeX, int biomeY, int biomeZ, ServerLevel world, BiomeSource biomeSource, Climate.ParameterList<Holder<Biome>> biomeEntries, Climate.Sampler noise) {
        return ModernerBetaCompatNeoForge.describe(context, biomeX, biomeY, biomeZ, world, biomeSource, biomeEntries, noise);
    }

    @Override
    public TerraBlenderCompat getTerraBlenderCompat() {
        return COMPAT_TERRABLENDER;
    }

    @Override
    public void registerCommandRegistrationCallback(TriFunction<CommandDispatcher<CommandSourceStack>, CommandBuildContext, Commands.CommandSelection, LiteralCommandNode<CommandSourceStack>> callback) {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> callback.apply(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
    }

    public static final DeferredRegister<MapCodec<? extends SurfaceRules.ConditionSource>> MATERIAL_CONDITIONS = DeferredRegister.create(BuiltInRegistries.MATERIAL_CONDITION, Biolith.MOD_ID);

    @Override
    public void registerMaterialCondition(String name, MapCodec<? extends SurfaceRules.ConditionSource> codec) {
        MATERIAL_CONDITIONS.register(name, () -> codec);
    }
}