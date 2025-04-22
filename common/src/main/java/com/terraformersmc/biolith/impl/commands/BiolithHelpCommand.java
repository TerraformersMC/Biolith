package com.terraformersmc.biolith.impl.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class BiolithHelpCommand {
    protected static int noargs(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.translatable("biolith.command.hint"));

        return 1;
    }

    protected static int help(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.translatable("biolith.command.help"));

        return 1;
    }

    protected static int helpSpecific(CommandContext<ServerCommandSource> context) {
        String subcommand = StringArgumentType.getString(context, "command");

        if (subcommand.compareTo("help") == 0) {
            // Very Funny
            return help(context);
        }

        if (BiolithCommands.COMMANDS.contains(subcommand)) {
            context.getSource().sendMessage(Text.translatable("biolith.command.help." + subcommand));
            return 1;
        } else {
            context.getSource().sendMessage(Text.translatable("biolith.command.help.missing"));
            return -1;
        }
    }
}
