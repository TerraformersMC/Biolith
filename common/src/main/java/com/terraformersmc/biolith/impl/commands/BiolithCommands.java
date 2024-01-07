package com.terraformersmc.biolith.impl.commands;

import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.platform.Services;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.command.argument.EntityArgumentType.entity;
import static net.minecraft.server.command.CommandManager.*;

public class BiolithCommands {
    protected static List<String> COMMANDS = List.of("help", "describe");

    public static void init() {
        if (!Biolith.getConfigManager().getGeneralConfig().areCommandsEnabled()) {
            return;
        }

        Services.PLATFORM.registerCommandRegistrationCallback((dispatcher, registryAccess, environment) -> dispatcher.register(literal("biolith")
                .then(literal("help")
                        .then(argument("command", word())
                                .executes(BiolithHelpCommand::helpSpecific))
                        .executes(BiolithHelpCommand::help))
                .then(literal("describe")
                        .then(literal("at")
                                .then(argument("entity", entity())
                                        .executes(BiolithDescribeCommand::atEntity))
                                .then(argument("position", blockPos())
                                        .executes(BiolithDescribeCommand::atPosition)))
                        .executes(BiolithDescribeCommand::atCaller))
                .executes(BiolithHelpCommand::noargs))
        );
    }
}
