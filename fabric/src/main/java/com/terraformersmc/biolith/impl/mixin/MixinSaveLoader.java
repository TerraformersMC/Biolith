package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=WorldStem.class, priority = 500)
public class MixinSaveLoader {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Record;<init>()V", shift = At.Shift.AFTER))
    private void biolith$earlyCaptureRegistries(CloseableResourceManager lifecycledResourceManager, ReloadableServerResources dataPackContents, LayeredRegistryAccess<RegistryLayer> combinedDynamicRegistries, WorldData saveProperties, CallbackInfo ci) {
        // Capture the registries ridiculously early on Fabric because BClib does,
        // and immediately forces us to provide biome entries...
        BiomeCoordinator.setRegistryManager(combinedDynamicRegistries);
    }
}
