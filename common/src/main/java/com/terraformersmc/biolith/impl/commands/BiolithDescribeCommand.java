package com.terraformersmc.biolith.impl.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.*;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.compat.VanillaCompat;
import com.terraformersmc.biolith.impl.platform.Services;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

public class BiolithDescribeCommand {
    protected static int atCaller(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().isExecutedByPlayer()) {
            context.getSource().sendMessage(Text.translatable("biolith.command.describe.nonPlayer").formatted(Formatting.RED));

            return -1;
        }

        return atBlockPos(context, context.getSource().getPlayerOrThrow().getBlockPos());
    }

    protected static int atEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return atBlockPos(context, EntityArgumentType.getEntity(context, "entity").getBlockPos());
    }

    protected static int atPosition(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return atBlockPos(context, BlockPosArgumentType.getValidBlockPos(context, "position"));
    }

    private static int atBlockPos(CommandContext<ServerCommandSource> context, BlockPos pos) {
        int biomeX = BiomeCoords.fromBlock(pos.getX());
        int biomeY = BiomeCoords.fromBlock(pos.getY());
        int biomeZ = BiomeCoords.fromBlock(pos.getZ());

        ServerWorld world = context.getSource().getWorld();
        if (world == null) {
            world = context.getSource().getServer().getOverworld();
        }
        BiomeSource biomeSource = world.getChunkManager().getChunkGenerator().getBiomeSource();
        MultiNoiseUtil.Entries<RegistryEntry<Biome>> biomeEntries = biomeSource.biolith$getBiomeEntries();
        if (biomeEntries == null) {
            context.getSource().sendMessage(Text.translatable("biolith.command.describe.notOurs").formatted(Formatting.RED));

            return -1;
        }
        MultiNoiseUtil.MultiNoiseSampler noise = world.getChunkManager().getNoiseConfig().getMultiNoiseSampler();

        // Describe Modern Beta worldgen if it's active.
        if (BiolithCompat.COMPAT_MODERN_BETA) {
            int mbStatus = Services.PLATFORM.describeModernBeta(context, biomeX, biomeY, biomeZ, world, biomeSource, biomeEntries, noise);
            if (mbStatus != 0) {
                /*
                 * mbStatus == 0 means Modern Beta although present is not generating this world
                 * Other values are the return value from our MB compat describe functionality
                 * (meaning, for better or worse, describe is done)
                 */
                return mbStatus;
            }
        }

        /*
         * Gather data
         */

        MultiNoiseUtil.NoiseValuePoint noisePoint;
        double replacementNoise;
        int replacementScale;

        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes = null;
        BiolithFittestNodes<RegistryEntry<Biome>> terrablenderFittestNodes = null;
        BiolithFittestNodes<RegistryEntry<Biome>> vanillaFittestNodes;
        DescribeBiomeData describeBiomeData;

        if (world.getDimensionEntry().matchesKey(DimensionTypes.OVERWORLD)) {
            noisePoint = noise.sample(biomeX, biomeY, biomeZ);
            vanillaFittestNodes = VanillaCompat.getBiome(noisePoint, biomeEntries);
            if (BiolithCompat.COMPAT_TERRABLENDER) {
                fittestNodes = terrablenderFittestNodes = Services.PLATFORM.getTerraBlenderCompat().getBiome(biomeX, biomeY, biomeZ, noisePoint, biomeEntries);
            }
            if (fittestNodes == null) {
                fittestNodes = vanillaFittestNodes;
            }
            replacementNoise = BiomeCoordinator.OVERWORLD.getLocalNoise(biomeX, biomeY, biomeZ);
            replacementScale = Biolith.getConfigManager().getGeneralConfig().getOverworldReplacementScale();
            describeBiomeData = BiomeCoordinator.OVERWORLD.getBiomeData(biomeX, biomeY, biomeZ, noisePoint, fittestNodes);
        } else if (world.getDimensionEntry().matchesKey(DimensionTypes.THE_NETHER)) {
            noisePoint = noise.sample(biomeX, biomeY, biomeZ);
            vanillaFittestNodes = VanillaCompat.getBiome(noisePoint, biomeEntries);
            if (BiolithCompat.COMPAT_TERRABLENDER) {
                fittestNodes = terrablenderFittestNodes = Services.PLATFORM.getTerraBlenderCompat().getBiome(biomeX, biomeY, biomeZ, noisePoint, biomeEntries);
            }
            if (fittestNodes == null) {
                fittestNodes = vanillaFittestNodes;
            }
            replacementNoise = BiomeCoordinator.NETHER.getLocalNoise(biomeX, biomeY, biomeZ);
            replacementScale = Biolith.getConfigManager().getGeneralConfig().getNetherReplacementScale();
            describeBiomeData = BiomeCoordinator.NETHER.getBiomeData(biomeX, biomeY, biomeZ, noisePoint, fittestNodes);
        } else if (world.getDimensionEntry().matchesKey(DimensionTypes.THE_END)) {
            RegistryEntry<Biome> original = VanillaCompat.getOriginalEndBiome(biomeX, biomeY, biomeZ, noise);
            noisePoint = BiomeCoordinator.END.sampleEndNoise(biomeX, biomeY, biomeZ, noise, original);
            vanillaFittestNodes = new BiolithFittestNodes<>(
                    new MultiNoiseUtil.SearchTree.TreeLeafNode<>(DimensionBiomePlacement.OUT_OF_RANGE,
                            VanillaCompat.getOriginalEndBiome(biomeX, biomeY, biomeZ, noise)), 0L);
            if (BiolithCompat.COMPAT_TERRABLENDER) {
                biomeSource.biolith$setBypass(true);
                fittestNodes = terrablenderFittestNodes = new BiolithFittestNodes<>(
                        new MultiNoiseUtil.SearchTree.TreeLeafNode<>(DimensionBiomePlacement.OUT_OF_RANGE,
                                biomeSource.getBiome(biomeX, biomeY, biomeZ, noise)), 0L);
                biomeSource.biolith$setBypass(false);
            }
            if (fittestNodes == null) {
                fittestNodes = vanillaFittestNodes;
            }
            replacementNoise = BiomeCoordinator.END.getLocalNoise(biomeX, biomeY, biomeZ);
            replacementScale = Biolith.getConfigManager().getGeneralConfig().getEndReplacementScale();
            describeBiomeData = BiomeCoordinator.END.getBiomeData(biomeX, biomeY, biomeZ, noisePoint, fittestNodes);
        } else {
            context.getSource().sendMessage(Text.translatable("biolith.command.describe.notOurs").formatted(Formatting.RED));

            return -1;
        }

        // Minecraft does not provide translations for their dimensions.
        String worldTranslationKey = world.getRegistryKey().getValue().toTranslationKey();
        //noinspection IfCanBeSwitch
        if (worldTranslationKey.equals("minecraft.overworld")) {
            worldTranslationKey = "biolith.world.minecraft.overworld";
        } else if (worldTranslationKey.equals("minecraft.the_nether")) {
            worldTranslationKey = "biolith.world.minecraft.the_nether";
        } else if (worldTranslationKey.equals("minecraft.the_end")) {
            worldTranslationKey = "biolith.world.minecraft.the_end";
        }

        /*
         * Format output
         */

        context.getSource().sendMessage(Text.literal("§nBiolith ")
                .append(Text.translatable(worldTranslationKey).formatted(Formatting.UNDERLINE))
                .append(Text.literal("§n ("))
                .append(Text.translatable("biolith.command.describe.biome.scale").formatted(Formatting.UNDERLINE))
                .append(Text.literal("§n: " + replacementScale + ") "))
                .append(Text.translatable("biolith.command.describe.header").formatted(Formatting.UNDERLINE)));

        context.getSource().sendMessage(Text.literal(
                String.format("§2Co§r:%+05.3f  §8De§r:%+05.3f  §cEr§r:%+05.3f  §1Hu§r:%+05.3f",
                        MultiNoiseUtil.toFloat(noisePoint.continentalnessNoise()),
                        MultiNoiseUtil.toFloat(noisePoint.depth()),
                        MultiNoiseUtil.toFloat(noisePoint.erosionNoise()),
                        MultiNoiseUtil.toFloat(noisePoint.humidityNoise())
                )));
        context.getSource().sendMessage(Text.literal(
                String.format("§7PV§r:%+05.3f  §4Te§r:%+05.3f  §5We§r:%+05.3f  §6BR§r:%+05.3f",
                        MultiNoiseUtil.toFloat(SubBiomeMatcherImpl.pvFromWeirdness(noisePoint.weirdnessNoise())),
                        MultiNoiseUtil.toFloat(noisePoint.temperatureNoise()),
                        MultiNoiseUtil.toFloat(noisePoint.weirdnessNoise()),
                        replacementNoise
                )));

        context.getSource().sendMessage(Text.translatable("biolith.command.describe.biome.vanilla")
                .append(textFromFittestNodes(vanillaFittestNodes)));

        if (terrablenderFittestNodes != null) {
            context.getSource().sendMessage(Text.translatable("biolith.command.describe.biome.terrablender")
                    .append(textFromFittestNodes(terrablenderFittestNodes)));
        }

        if (describeBiomeData.replacementBiome != null) {
            context.getSource().sendMessage(Text.translatable("biolith.command.describe.biome.replacement")
                    .append(textFromBiome(describeBiomeData.replacementBiome))
                    .append(Text.literal("\n    "))
                    .append(describeBiomeData.lowerBiome == null ?
                            Text.translatable("biolith.command.describe.biome.none") :
                            textFromBiome(describeBiomeData.lowerBiome))
                    .append(Text.literal(" < "))
                    .append(textFromBiome(describeBiomeData.replacementBiome))
                    .append(Text.literal(" < "))
                    .append(describeBiomeData.higherBiome == null ?
                            Text.translatable("biolith.command.describe.biome.none") :
                            textFromBiome(describeBiomeData.higherBiome))
                    .append(Text.literal(String.format("\n    %+05.3f < %+05.3f < %+05.3f ",
                            describeBiomeData.replacementRange.x,
                            replacementNoise,
                            describeBiomeData.replacementRange.y))));
        }

        if (describeBiomeData.subBiome != null) {
            context.getSource().sendMessage(Text.translatable("biolith.command.describe.biome.sub")
                    .append(textFromBiome(describeBiomeData.subBiome)));
        }

        return 1;
    }

    public static MutableText textFromFittestNodes(BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes) {
        MutableText text = textFromBiome(fittestNodes.ultimate());

        if (fittestNodes.penultimate() != null) {
            text = text.append(Text.literal("\n    "))
                    .append(Text.translatable("biolith.command.describe.biome.nearest"))
                    .append(textFromBiome(fittestNodes.penultimate()))
                    .append(" (+" + (fittestNodes.penultimateDistance() - fittestNodes.ultimateDistance()) + ")");
        }

        return text;
    }

    public static MutableText textFromBiome(MultiNoiseUtil.SearchTree.TreeLeafNode<RegistryEntry<Biome>> leafNode) {
        return textFromBiome(leafNode.value.getKey().orElseThrow());
    }

    public static MutableText textFromBiome(RegistryEntry<Biome> biome) {
        return textFromBiome(biome.getKey().orElseThrow());
    }

    public static MutableText textFromBiome(RegistryKey<Biome> biome) {
        return Text.translatable(biome.getValue().toTranslationKey("biome"));
    }

    // Ferries back data from DimensionalBiomePlacement.getBiomeData().
    public record DescribeBiomeData(
            @Nullable Vector2f replacementRange,
            @Nullable RegistryKey<Biome> replacementBiome,
            @Nullable RegistryKey<Biome> lowerBiome,
            @Nullable RegistryKey<Biome> higherBiome,
            @Nullable RegistryKey<Biome> subBiome
    ) {}
}
