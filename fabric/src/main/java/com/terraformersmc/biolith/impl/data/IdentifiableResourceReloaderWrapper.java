package com.terraformersmc.biolith.impl.data;

import com.terraformersmc.biolith.impl.Biolith;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/*
 * This wrapper class turns a ResourceReloader into a Fabric IdentifiableResourceReloader.
 */
public record IdentifiableResourceReloaderWrapper(Identifier identifier, ResourceReloader reloader) implements IdentifiableResourceReloadListener {
    public IdentifiableResourceReloaderWrapper(String name, ResourceReloader reloader) {
        this(Identifier.of(Biolith.MOD_ID, name), reloader);
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return reloader.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
    }

    @Override
    public String getName() {
        return reloader.getClass().getSimpleName();
    }

    @Override
    public Identifier getFabricId() {
        return identifier;
    }
}
