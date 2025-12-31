package com.terraformersmc.biolith.impl.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.api.biome.sub.BiomeParameterTargets;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.*;
import com.terraformersmc.biolith.impl.compat.BiolithCompat;
import com.terraformersmc.biolith.impl.compat.VanillaCompat;
import com.terraformersmc.biolith.impl.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.jetbrains.annotations.Nullable;

public class BiolithDescribeCommand {
    protected static int atCaller(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.nonPlayer").withStyle(ChatFormatting.RED));

            return -1;
        }

        return atBlockPos(context, context.getSource().getPlayerOrException().blockPosition());
    }

    protected static int atEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return atBlockPos(context, EntityArgument.getEntity(context, "entity").blockPosition());
    }

    protected static int atPosition(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return atBlockPos(context, BlockPosArgument.getSpawnablePos(context, "position"));
    }

    private static int atBlockPos(CommandContext<CommandSourceStack> context, BlockPos pos) {
        int biomeX = QuartPos.fromBlock(pos.getX());
        int biomeY = QuartPos.fromBlock(pos.getY());
        int biomeZ = QuartPos.fromBlock(pos.getZ());

        ServerLevel world = context.getSource().getLevel();
        if (world == null) {
            world = context.getSource().getServer().overworld();
        }
        BiomeSource biomeSource = world.getChunkSource().getGenerator().getBiomeSource();
        Climate.ParameterList<Holder<Biome>> biomeEntries = biomeSource.biolith$getBiomeEntries();
        if (biomeEntries == null) {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.notOurs").withStyle(ChatFormatting.RED));

            return -1;
        }
        Climate.Sampler noise = world.getChunkSource().randomState().sampler();

        // Describe Moderner Beta worldgen if it's active.
        if (BiolithCompat.COMPAT_MODERNER_BETA) {
            int mbStatus = Services.PLATFORM.describeModernerBeta(context, biomeX, biomeY, biomeZ, world, biomeSource, biomeEntries, noise);
            if (mbStatus != 0) {
                /*
                 * mbStatus == 0 means Moderner Beta although present is not generating this world
                 * Other values are the return value from our MB compat describe functionality
                 * (meaning, for better or worse, describe is done)
                 */
                return mbStatus;
            }
        }

        /*
         * Gather data
         */

        Climate.TargetPoint noisePoint;
        double replacementNoise;
        int replacementScale;

        BiolithFittestNodes<Holder<Biome>> fittestNodes = null;
        BiolithFittestNodes<Holder<Biome>> terrablenderFittestNodes = null;
        BiolithFittestNodes<Holder<Biome>> vanillaFittestNodes;
        DescribeBiomeData describeBiomeData;

        if (world.dimensionTypeRegistration().is(BuiltinDimensionTypes.OVERWORLD)) {
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
        } else if (world.dimensionTypeRegistration().is(BuiltinDimensionTypes.NETHER)) {
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
        } else if (world.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
            Holder<Biome> original = VanillaCompat.getOriginalEndBiome(biomeX, biomeY, biomeZ, noise);
            noisePoint = BiomeCoordinator.END.sampleEndNoise(biomeX, biomeY, biomeZ, noise, original);
            vanillaFittestNodes = VanillaCompat.getEndBiome(noisePoint, biomeEntries, original);
            if (BiolithCompat.COMPAT_TERRABLENDER) {
                biomeSource.biolith$setBypass(true);
                fittestNodes = terrablenderFittestNodes = new BiolithFittestNodes<>(
                        new Climate.RTree.Leaf<>(DimensionBiomePlacement.OUT_OF_RANGE,
                                biomeSource.getNoiseBiome(biomeX, biomeY, biomeZ, noise)), 0L);
                biomeSource.biolith$setBypass(false);
            }
            if (fittestNodes == null) {
                fittestNodes = vanillaFittestNodes;
            }
            replacementNoise = BiomeCoordinator.END.getLocalNoise(biomeX, biomeY, biomeZ);
            replacementScale = Biolith.getConfigManager().getGeneralConfig().getEndReplacementScale();
            describeBiomeData = BiomeCoordinator.END.getBiomeData(biomeX, biomeY, biomeZ, noisePoint, fittestNodes);
        } else {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.notOurs").withStyle(ChatFormatting.RED));

            return -1;
        }

        // Minecraft does not provide translations for their dimensions.
        String worldTranslationKey = world.dimension().identifier().toLanguageKey();
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

        context.getSource().sendSystemMessage(Component.literal("§nBiolith ")
                .append(Component.translatable(worldTranslationKey).withStyle(ChatFormatting.UNDERLINE))
                .append(Component.literal("§n ("))
                .append(Component.translatable("biolith.command.describe.biome.scale").withStyle(ChatFormatting.UNDERLINE))
                .append(Component.literal("§n: " + replacementScale + ") "))
                .append(Component.translatable("biolith.command.describe.header").withStyle(ChatFormatting.UNDERLINE)));

        context.getSource().sendSystemMessage(Component.literal(
                String.format("§2Co§r:%+05.3f  §8De§r:%+05.3f  §cEr§r:%+05.3f  §1Hu§r:%+05.3f",
                        Climate.unquantizeCoord(noisePoint.continentalness()),
                        Climate.unquantizeCoord(noisePoint.depth()),
                        Climate.unquantizeCoord(noisePoint.erosion()),
                        Climate.unquantizeCoord(noisePoint.humidity())
                )));
        context.getSource().sendSystemMessage(Component.literal(
                String.format("§7PV§r:%+05.3f  §4Te§r:%+05.3f  §5We§r:%+05.3f  §6BR§r:%+05.3f",
                        Climate.unquantizeCoord(BiomeParameterTargets.getPeaksValleysNoiseLong(noisePoint.weirdness())),
                        Climate.unquantizeCoord(noisePoint.temperature()),
                        Climate.unquantizeCoord(noisePoint.weirdness()),
                        replacementNoise
                )));

        context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.biome.vanilla")
                .append(textFromFittestNodes(vanillaFittestNodes)));

        if (terrablenderFittestNodes != null) {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.biome.terrablender")
                    .append(textFromFittestNodes(terrablenderFittestNodes)));
        }

        if (describeBiomeData.replacementBiome != null && describeBiomeData.replacementRange == null) {
            // Impossible, but this helps to convince IDEA
            return -2;
        }

        if (describeBiomeData.replacementBiome != null) {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.biome.replacement")
                    .append(textFromBiome(describeBiomeData.replacementBiome))
                    .append(Component.literal("\n    "))
                    .append(describeBiomeData.lowerBiome == null ?
                            Component.translatable("biolith.command.describe.biome.none") :
                            textFromBiome(describeBiomeData.lowerBiome))
                    .append(Component.literal(" < "))
                    .append(textFromBiome(describeBiomeData.replacementBiome))
                    .append(Component.literal(" < "))
                    .append(describeBiomeData.higherBiome == null ?
                            Component.translatable("biolith.command.describe.biome.none") :
                            textFromBiome(describeBiomeData.higherBiome))
                    .append(Component.literal(String.format("\n    %+05.3f < %+05.3f < %+05.3f ",
                            describeBiomeData.replacementRange.minInclusive(),
                            replacementNoise,
                            describeBiomeData.replacementRange.maxInclusive()))));
        }

        if (describeBiomeData.subBiome != null) {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.biome.sub")
                    .append(textFromBiome(describeBiomeData.subBiome)));
        }

        return 1;
    }

    public static MutableComponent textFromFittestNodes(BiolithFittestNodes<Holder<Biome>> fittestNodes) {
        MutableComponent text = textFromBiome(fittestNodes.ultimate());

        if (fittestNodes.penultimate() != null) {
            text = text.append(Component.literal("\n    "))
                    .append(Component.translatable("biolith.command.describe.biome.nearest"))
                    .append(textFromBiome(fittestNodes.penultimate()))
                    .append(" (+" + (fittestNodes.penultimateDistance() - fittestNodes.ultimateDistance()) + ")");
        }

        return text;
    }

    public static MutableComponent textFromBiome(Climate.RTree.Leaf<Holder<Biome>> leafNode) {
        return textFromBiome(leafNode.value.unwrapKey().orElseThrow());
    }

    public static MutableComponent textFromBiome(Holder<Biome> biome) {
        return textFromBiome(biome.unwrapKey().orElseThrow());
    }

    public static MutableComponent textFromBiome(ResourceKey<Biome> biome) {
        return Component.translatable(biome.identifier().toLanguageKey("biome"));
    }

    // Ferries back data from DimensionalBiomePlacement.getBiomeData().
    public record DescribeBiomeData(
            @Nullable InclusiveRange<Float> replacementRange,
            @Nullable ResourceKey<Biome> replacementBiome,
            @Nullable ResourceKey<Biome> lowerBiome,
            @Nullable ResourceKey<Biome> higherBiome,
            @Nullable ResourceKey<Biome> subBiome
    ) {}
}
