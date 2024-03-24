package com.terraformersmc.biolith.impl.biome;

import com.terraformersmc.biolith.impl.Biolith;

public class NetherBiomePlacement extends DimensionBiomePlacement {
    private final double[] scale = new double[5];

    public NetherBiomePlacement() {
        super();

        int configScale = Biolith.getConfigManager().getGeneralConfig().getNetherReplacementScale();
        scale[0] = 256 * configScale;
        scale[1] =  64 * configScale;
        scale[2] =  32 * configScale;
        scale[3] =   8 * configScale;
        scale[4] =   2 * configScale;
    }

    @Override
    public double getLocalNoise(int x, int y, int z) {
        double localNoise;

        // Three octaves to give some edge fuzz
        localNoise  = replacementNoise.sample((double)(x + seedlets[0]) / scale[0], (double)(y + seedlets[0]) / scale[1], (double)(z + seedlets[1]) / scale[0]);
        localNoise += replacementNoise.sample((double)(x + seedlets[4]) / scale[2], (double)(y + seedlets[0]) / scale[3], (double)(z + seedlets[5]) / scale[2]) / 16D;
        localNoise += replacementNoise.sample((double)(x + seedlets[6]) / scale[3], (double)(y + seedlets[0]) / scale[4], (double)(z + seedlets[7]) / scale[3]) / 32D;

        // Scale the result back to amplitude 1 and then normalize
        localNoise = normalize(localNoise / 1.09375D);

        return localNoise;
    }
}
