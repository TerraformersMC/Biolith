package com.terraformersmc.biolith.impl.platform;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompat;
import com.terraformersmc.biolith.impl.compat.TerraBlenderCompatForge;
import com.terraformersmc.biolith.impl.platform.services.PlatformHelper;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.function.TriFunction;

import java.nio.file.Path;

public class ForgePlatformHelper implements PlatformHelper {
    private static final TerraBlenderCompat COMPAT_TERRABLENDER = new TerraBlenderCompatForge();

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public TerraBlenderCompat getTerraBlenderCompat() {
        return COMPAT_TERRABLENDER;
    }

    @Override
    public void registerCommandRegistrationCallback(TriFunction<CommandDispatcher<CommandSourceStack>, CommandBuildContext, Commands.CommandSelection, LiteralCommandNode<CommandSourceStack>> callback) {
        RegisterCommandsEvent.BUS.addListener(event -> callback.apply(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));
    }
}