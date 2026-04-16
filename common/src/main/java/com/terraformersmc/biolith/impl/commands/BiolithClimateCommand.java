package com.terraformersmc.biolith.impl.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraformersmc.biolith.impl.mixin.AccessorBiome;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class BiolithClimateCommand {

    protected static int atCaller(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.nonPlayer").withStyle(ChatFormatting.RED));

            return -1;
        }

        return getClimate(context, context.getSource().getPlayerOrException().level(), context.getSource().getPlayerOrException().blockPosition());
    }

    protected static int atPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return getClimate(context, context.getSource().getPlayerOrException().level(), BlockPosArgument.getSpawnablePos(context, "position"));
    }

    protected static int atEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return getClimate(context, EntityArgument.getEntity(context, "entity").level(), EntityArgument.getEntity(context, "entity").blockPosition());
    }

    static int getClimate(CommandContext<CommandSourceStack> context, Level level, BlockPos pos) {
        int seaLevel = level.getSeaLevel();

        Biome biome = level.getBiome(pos).value();

        float baseTemp = biome.getBaseTemperature();
        float adjustedTemp = biome.getHeightAdjustedTemperature(pos, seaLevel);

        Biome.ClimateSettings climate = ((AccessorBiome) (Object) biome).getClimate();
        float downfall = climate.downfall();

        ResourceKey<Biome> biomeKey = level.getBiome(pos).unwrapKey().get();

        context.getSource().sendSystemMessage(Component.literal("")
                .append(Component.translatable("biolith.command.climate.at").append(Component.literal(" " + pos + "\n"))
                        .withStyle(ChatFormatting.WHITE)
                .append(Component.literal("  ").append(Component.translatable("biolith.command.climate.biome")).append(Component.literal(": " + biomeKey.identifier() + "\n"))
                        .withStyle(ChatFormatting.GRAY))
                        .append("  ").append(Component.translatable("biolith.command.climate.local_temperature").append(String.format(": %.3f\n", adjustedTemp))
                        .withStyle(ChatFormatting.AQUA))
                        .append("  ").append(Component.translatable("biolith.command.climate.biome_temperature").append(String.format(": %.3f\n", baseTemp))
                        .withStyle(ChatFormatting.GREEN))
                        .append("  ").append(Component.translatable("biolith.command.climate.biome_downfall").append(String.format(": %.3f\n", downfall))
                        .withStyle(ChatFormatting.LIGHT_PURPLE))));
        return 1;
    }
}