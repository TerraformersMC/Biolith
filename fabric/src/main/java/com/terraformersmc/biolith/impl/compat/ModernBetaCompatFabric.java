package com.terraformersmc.biolith.impl.compat;

import com.mojang.brigadier.context.CommandContext;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import com.terraformersmc.biolith.impl.commands.BiolithDescribeCommand;
import mod.bespectacled.modernbeta.world.biome.ModernBetaBiomeSource;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import static com.terraformersmc.biolith.impl.commands.BiolithDescribeCommand.textFromBiome;

public class ModernBetaCompatFabric {
    public static int describe(CommandContext<ServerCommandSource> context, int biomeX, int biomeY, int biomeZ, ServerWorld world, BiomeSource biomeSource, MultiNoiseUtil.Entries<RegistryEntry<Biome>> biomeEntries, MultiNoiseUtil.MultiNoiseSampler noise) {
        if (!(biomeSource instanceof ModernBetaBiomeSource)) {
            return 0;
        }

        RegistryEntry<Biome> original = ((ModernBetaBiomeSource) biomeSource).getBiomeProvider().getBiome(biomeX, biomeY, biomeZ);
        BiolithFittestNodes<RegistryEntry<Biome>> fittestNodes = new BiolithFittestNodes<>(new MultiNoiseUtil.SearchTree.TreeLeafNode<>(DimensionBiomePlacement.OUT_OF_RANGE, original), 0, null, Integer.MAX_VALUE);

        double replacementNoise = BiomeCoordinator.OVERWORLD.getLocalNoise(biomeX, biomeY, biomeZ);
        int replacementScale = Biolith.getConfigManager().getGeneralConfig().getOverworldReplacementScale();

        BiolithDescribeCommand.DescribeBiomeData describeBiomeData = BiomeCoordinator.OVERWORLD.getBiomeData(biomeX, biomeY, biomeZ, new MultiNoiseUtil.NoiseValuePoint(0,0,0,0,0,0), fittestNodes);

        String worldTranslationKey = world.getRegistryKey().getValue().toTranslationKey();

        context.getSource().sendMessage(Text.literal("§nBiolith ")
                .append(Text.translatable(worldTranslationKey).formatted(Formatting.UNDERLINE))
                .append(Text.literal("§n ("))
                .append(Text.translatable("biolith.command.describe.biome.scale").formatted(Formatting.UNDERLINE))
                .append(Text.literal("§n: " + replacementScale + ") "))
                .append(Text.translatable("biolith.command.describe.header").formatted(Formatting.UNDERLINE)));

        context.getSource().sendMessage(Text.literal(String.format("§6BR§r:%+05.3f", replacementNoise)));

        context.getSource().sendMessage(Text.translatable("biolith.command.describe.biome.modern_beta")
                .append(textFromBiome(original)));

        if (describeBiomeData.replacementBiome() != null) {
            context.getSource().sendMessage(Text.translatable("biolith.command.describe.biome.replacement")
                    .append(textFromBiome(describeBiomeData.replacementBiome()))
                    .append(Text.literal("\n    "))
                    .append(describeBiomeData.lowerBiome() == null ?
                            Text.translatable("biolith.command.describe.biome.none") :
                            textFromBiome(describeBiomeData.lowerBiome()))
                    .append(Text.literal(" < "))
                    .append(textFromBiome(describeBiomeData.replacementBiome()))
                    .append(Text.literal(" < "))
                    .append(describeBiomeData.higherBiome() == null ?
                            Text.translatable("biolith.command.describe.biome.none") :
                            textFromBiome(describeBiomeData.higherBiome()))
                    .append(Text.literal(String.format("\n    %+05.3f < %+05.3f < %+05.3f ",
                            describeBiomeData.replacementRange().x,
                            replacementNoise,
                            describeBiomeData.replacementRange().y))));
        }

        return 1;
    }
}