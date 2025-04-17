package com.terraformersmc.biolith.impl.data;

import com.terraformersmc.biolith.impl.Biolith;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

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
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
        return reloader.reload(synchronizer, manager, prepareExecutor, applyExecutor);
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
