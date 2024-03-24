package com.terraformersmc.biolith.impl.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.terraformersmc.biolith.impl.Biolith;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import com.terraformersmc.biolith.impl.surface.SurfaceRuleCollector;
import net.minecraft.resource.Resource;
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

public class SurfaceGenerationLoader extends SinglePreparationResourceReloader<List<SurfaceGenerationMarshaller>> {
    public static final String RESOURCE_PATH = "biolith/surface_generation.json";

    @Override
    protected List<SurfaceGenerationMarshaller> prepare(ResourceManager manager, Profiler profiler) {
        profiler.startTick();
        List<SurfaceGenerationMarshaller> marshallers = new ArrayList<>();

        for (String namespace : manager.getAllNamespaces()) {
            profiler.push(namespace);
            try {
                for (Resource resource : manager.getAllResources(new Identifier(namespace, RESOURCE_PATH))) {
                    profiler.push(resource.getPack().getName());
                    try {
                        InputStream inputStream = resource.getInputStream();
                        try {
                            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                            try {
                                profiler.push("parse");

                                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                                SurfaceGenerationMarshaller marshaller = get(SurfaceGenerationMarshaller.CODEC, jsonObject);
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
                        Biolith.LOGGER.warn("Invalid {} in resourcepack: '{}'", RESOURCE_PATH, resource.getPack().getName(), runtimeBreak);
                    }
                    profiler.pop();
                }
            } catch (IOException ignored) {
                // No surface generation
            }
            profiler.pop();
        }

        profiler.endTick();
        return marshallers;
    }

    @Override
    protected void apply(List<SurfaceGenerationMarshaller> marshallers, ResourceManager manager, Profiler profiler) {
        if (BiomeCoordinator.isServerStarted()) {
            Biolith.LOGGER.warn("Ignoring request to reload surface generation data while server is running.");
            return;
        }

        if (marshallers.size() > 0) {
            Biolith.LOGGER.info("Applying surface generation data from {} source(s).", marshallers.size());
        }

        SurfaceRuleCollector.OVERWORLD.clearFromData();
        SurfaceRuleCollector.NETHER.clearFromData();
        SurfaceRuleCollector.END.clearFromData();

        for (SurfaceGenerationMarshaller marshaller : marshallers) {
            marshaller.unmarshall();
        }
    }

    public static <R> R get(Decoder<R> decoder, JsonElement jsonElement) throws NullPointerException {
        return decoder.parse(JsonOps.INSTANCE, jsonElement).result().orElseThrow();
    }
}
