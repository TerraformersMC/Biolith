package com.terraformersmc.biolith.impl.compat;

import com.mojang.brigadier.context.CommandContext;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import com.terraformersmc.biolith.impl.commands.BiolithDescribeCommand;
import mod.bluestaggo.modernerbeta.level.biome.ModernBetaBiomeSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import static com.terraformersmc.biolith.impl.commands.BiolithDescribeCommand.textFromBiome;

public class ModernerBetaCompatNeoForge {
    public static int describe(CommandContext<CommandSourceStack> context, int biomeX, int biomeY, int biomeZ, ServerLevel world, BiomeSource biomeSource, Climate.ParameterList<Holder<Biome>> biomeEntries, Climate.Sampler noise) {
        if (!(biomeSource instanceof ModernBetaBiomeSource)) {
            return 0;
        }

        Holder<Biome> original = ((ModernBetaBiomeSource) biomeSource).getBiomeProvider().getBiome(biomeX, biomeY, biomeZ);
        BiolithFittestNodes<Holder<Biome>> fittestNodes = new BiolithFittestNodes<>(new Climate.RTree.Leaf<>(DimensionBiomePlacement.OUT_OF_RANGE, original), 0, null, Integer.MAX_VALUE);

        double replacementNoise = BiomeCoordinator.OVERWORLD.getLocalNoise(biomeX, biomeY, biomeZ);
        int replacementScale = Biolith.getConfigManager().getGeneralConfig().getOverworldReplacementScale();

        BiolithDescribeCommand.DescribeBiomeData describeBiomeData = BiomeCoordinator.OVERWORLD.getBiomeData(biomeX, biomeY, biomeZ, new Climate.TargetPoint(0,0,0,0,0,0), fittestNodes);

        String worldTranslationKey = world.dimension().identifier().toLanguageKey();

        context.getSource().sendSystemMessage(Component.literal("§nBiolith ")
                .append(Component.translatable(worldTranslationKey).withStyle(ChatFormatting.UNDERLINE))
                .append(Component.literal("§n ("))
                .append(Component.translatable("biolith.command.describe.biome.scale").withStyle(ChatFormatting.UNDERLINE))
                .append(Component.literal("§n: " + replacementScale + ") "))
                .append(Component.translatable("biolith.command.describe.header").withStyle(ChatFormatting.UNDERLINE)));

        context.getSource().sendSystemMessage(Component.literal(String.format("§6BR§r:%+05.3f", replacementNoise)));

        context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.biome.moderner_beta")
                .append(textFromBiome(original)));

        if (describeBiomeData.replacementBiome() != null && describeBiomeData.replacementRange() == null) {
            // Impossible, but this helps to convince IDEA
            return -2;
        }

        if (describeBiomeData.replacementBiome() != null) {
            context.getSource().sendSystemMessage(Component.translatable("biolith.command.describe.biome.replacement")
                    .append(textFromBiome(describeBiomeData.replacementBiome()))
                    .append(Component.literal("\n    "))
                    .append(describeBiomeData.lowerBiome() == null ?
                            Component.translatable("biolith.command.describe.biome.none") :
                            textFromBiome(describeBiomeData.lowerBiome()))
                    .append(Component.literal(" < "))
                    .append(textFromBiome(describeBiomeData.replacementBiome()))
                    .append(Component.literal(" < "))
                    .append(describeBiomeData.higherBiome() == null ?
                            Component.translatable("biolith.command.describe.biome.none") :
                            textFromBiome(describeBiomeData.higherBiome()))
                    .append(Component.literal(String.format("\n    %+05.3f < %+05.3f < %+05.3f ",
                            describeBiomeData.replacementRange().minInclusive(),
                            replacementNoise,
                            describeBiomeData.replacementRange().maxInclusive()))));
        }

        return 1;
    }
}
