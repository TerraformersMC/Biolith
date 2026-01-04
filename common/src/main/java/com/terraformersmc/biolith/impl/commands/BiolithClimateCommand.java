package com.terraformersmc.biolith.impl.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraformersmc.biolith.impl.mixin.AccessorBiome;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class BiolithClimateCommand {

    protected static int atCaller(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendMessage(Text.translatable("biolith.command.describe.nonPlayer").formatted(Formatting.RED));

            return -1;
        }

        return getClimate(context, context.getSource().getPlayerOrThrow().getEntityWorld(), context.getSource().getPlayerOrThrow().getBlockPos());
    }

    protected static int atPosition(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return getClimate(context, context.getSource().getPlayerOrThrow().getEntityWorld(), BlockPosArgumentType.getValidBlockPos(context, "position"));
    }

    protected static int atEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return getClimate(context, EntityArgumentType.getEntity(context, "entity").getEntityWorld(), EntityArgumentType.getEntity(context, "entity").getBlockPos());
    }

    static int getClimate(CommandContext<ServerCommandSource> context, World world, BlockPos pos) {
        int seaLevel = world.getSeaLevel();

        Biome biome = world.getBiome(pos).value();

        float baseTemp = biome.getTemperature();
        float adjustedTemp = biome.getTemperature(pos, seaLevel);

        Biome.Weather climate = ((AccessorBiome) (Object) biome).getClimate();
        float downfall = climate.downfall();

        RegistryKey<Biome> biomeKey = world.getBiome(pos).getKey().get();

        context.getSource().sendMessage(Text.literal("")
                .append(Text.translatable("biolith.command.climate.at").append(Text.literal(" " + pos + "\n"))
                        .formatted(Formatting.WHITE)
                .append(Text.literal("  ").append(Text.translatable("biolith.command.climate.biome")).append(Text.literal(": " + biomeKey.getValue() + "\n"))
                        .formatted(Formatting.GRAY))
                        .append("  ").append(Text.translatable("biolith.command.climate.local_temperature").append(String.format(": %.3f\n", adjustedTemp))
                        .formatted(Formatting.AQUA))
                        .append("  ").append(Text.translatable("biolith.command.climate.biome_temperature").append(String.format(": %.3f\n", baseTemp))
                        .formatted(Formatting.GREEN))
                        .append("  ").append(Text.translatable("biolith.command.climate.biome_downfall").append(String.format(": %.3f\n", downfall))
                        .formatted(Formatting.LIGHT_PURPLE))));
        return 1;
    }
}