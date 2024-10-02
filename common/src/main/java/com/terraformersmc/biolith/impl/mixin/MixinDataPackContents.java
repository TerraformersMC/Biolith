package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.terraformersmc.biolith.impl.data.BiomePlacementLoader;
import com.terraformersmc.biolith.impl.data.SurfaceGenerationLoader;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Stream;

@Mixin(DataPackContents.class)
public abstract class MixinDataPackContents {
    @Unique
    private BiomePlacementLoader biomePlacementLoader;
    @Unique
    private SurfaceGenerationLoader surfaceGenerationLoader;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void biolith$addDataPackContents(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, RegistryWrapper.WrapperLookup registries, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, List<Registry.PendingTagLoad<?>> pendingTagLoads, int functionPermissionLevel, CallbackInfo ci) {
        biomePlacementLoader = new BiomePlacementLoader();
        surfaceGenerationLoader = new SurfaceGenerationLoader();
    }

    @ModifyReturnValue(method = "getContents", at = @At("RETURN"))
    @SuppressWarnings("unused")
    private List<ResourceReloader> biolith$addReloadersToContents(List<ResourceReloader> original) {
        return Stream.concat(original.stream(), Stream.of(biomePlacementLoader, surfaceGenerationLoader)).toList();
    }
}
