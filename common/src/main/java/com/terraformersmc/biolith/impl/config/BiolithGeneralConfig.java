package com.terraformersmc.biolith.impl.config;

import com.terraformersmc.biolith.impl.Biolith;
import net.minecraft.util.math.MathHelper;

public class BiolithGeneralConfig {
	private boolean enableCommands = true;
	private int overworldReplacementScale = 4;
	private int netherReplacementScale = 2;
	private int endReplacementScale = 1;

	public boolean areCommandsEnabled() {
		return enableCommands;
	}

	public int getOverworldReplacementScale() {
		if (overworldReplacementScale < 1 || overworldReplacementScale > 16) {
			Biolith.LOGGER.warn("Biolith Overworld replacement noise scale is out of range; clamping to [1,16]...");
			overworldReplacementScale = MathHelper.clamp(overworldReplacementScale, 1, 16);
		}

		return overworldReplacementScale;
	}

	public int getNetherReplacementScale() {
		if (netherReplacementScale < 1 || netherReplacementScale > 16) {
			Biolith.LOGGER.warn("Biolith Nether replacement noise scale is out of range; clamping to [1,16]...");
			netherReplacementScale = MathHelper.clamp(netherReplacementScale, 1, 16);
		}

		return netherReplacementScale;
	}

	public int getEndReplacementScale() {
		if (endReplacementScale < 1 || endReplacementScale > 16) {
			Biolith.LOGGER.warn("Biolith End replacement noise scale is out of range; clamping to [1,16]...");
			endReplacementScale = MathHelper.clamp(endReplacementScale, 1, 16);
		}

		return endReplacementScale;
	}
}
