package com.terraformersmc.biolith.impl.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class BiolithHelpCommand {
    protected static int noargs(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("biolith.command.hint"));

        return 1;
    }

    protected static int help(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.translatable("biolith.command.help"));

        return 1;
    }

    protected static int helpSpecific(CommandContext<CommandSourceStack> context) {
        String subcommand = StringArgumentType.getString(context, "command");

        if (subcommand.compareTo("help") == 0) {
            // Very Funny
            return help(context);
        }

        if (BiolithCommands.COMMANDS.contains(subcommand)) {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.help." + subcommand));
            return 1;
        } else {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.help.missing"));
            return -1;
        }
    }
}
