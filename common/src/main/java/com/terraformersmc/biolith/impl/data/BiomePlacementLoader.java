package com.terraformersmc.biolith.impl.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BiomePlacementLoader extends SinglePreparationResourceReloader<List<BiomePlacementMarshaller>> {
    public static final ResourceFinder BIOME_PLACEMENT_FINDER = ResourceFinder.json("biolith/biome_placement");

    @Override
    protected List<BiomePlacementMarshaller> prepare(ResourceManager manager, Profiler profiler) {
        profiler.startTick();
        List<BiomePlacementMarshaller> marshallers = new ArrayList<>();

        try {
            for (Map.Entry<Identifier, Resource> entry : BIOME_PLACEMENT_FINDER.findResources(manager).entrySet()) {
                Resource resource = entry.getValue();

                profiler.push(resource.getPackId());
                try {
                    InputStream inputStream = resource.getInputStream();
                    try {
                        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                        try {
                            profiler.push("parse");

                            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                            BiomePlacementMarshaller marshaller = get(BiomePlacementMarshaller.CODEC, jsonObject);
                            if (marshaller != null) {
                                marshallers.add(marshaller);
                            } else {
                                throw new RuntimeException();
                            }

                            profiler.pop();
                        } catch (Throwable throwable) {
                            try {
                                reader.close();
                            } catch (Throwable closeBreak) {
                                throwable.addSuppressed(closeBreak);
                            }
                            throw throwable;
                        }
                        reader.close();
                    } catch (Throwable throwable) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable closeBreak) {
                                throwable.addSuppressed(closeBreak);
                            }
                        }
                        throw throwable;
                    }
                    inputStream.close();
                } catch (RuntimeException runtimeBreak) {
                    Biolith.LOGGER.warn("Parsing error loading biome placement '{}': '{}'", resource.getPackId(), runtimeBreak);
                }
                profiler.pop();
            }
        } catch (IOException ignored) {
            // No biome placement
        }
        profiler.pop();

        return marshallers;
    }

    @Override
    protected void apply(List<BiomePlacementMarshaller> marshallers, ResourceManager manager, Profiler profiler) {
        if (BiomeCoordinator.isServerStarted()) {
            Biolith.LOGGER.warn("Ignoring request to reload biome placement data while server is running.");
            return;
        }

        if (marshallers.size() > 0) {
            Biolith.LOGGER.info("Applying biome placement data from {} source(s).", marshallers.size());
        }

        BiomeCoordinator.OVERWORLD.clearFromData();
        BiomeCoordinator.NETHER.clearFromData();
        BiomeCoordinator.END.clearFromData();

        for (BiomePlacementMarshaller marshaller : marshallers) {
            marshaller.unmarshall();
        }
    }

    public static <R> R get(Decoder<R> decoder, JsonElement jsonElement) throws NullPointerException {
        return decoder.parse(JsonOps.INSTANCE, jsonElement).result().orElseThrow();
    }
}
