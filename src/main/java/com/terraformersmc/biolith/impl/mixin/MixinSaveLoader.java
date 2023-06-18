package com.terraformersmc.biolith.impl.mixin;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.SaveLoader;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value=SaveLoader.class, priority = 500)
public class MixinSaveLoader {
    @Shadow @Final private CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries;

    @ModifyVariable(method = "<init>", argsOnly = true, at = @At(value = "INVOKE", target = "Ljava/lang/Record;<init>()V", shift = At.Shift.AFTER))
    private CombinedDynamicRegistries<ServerDynamicRegistryType> biolith$earlyCaptureRegistries(
            CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries
    ) {
        System.out.println("Biolith: early capture registries");
        // We need the registries really early in case TerraBlender calls us before the Fabric server start event.
        BiomeCoordinator.setRegistryManager(combinedDynamicRegistries);
        return combinedDynamicRegistries;
    }
}
