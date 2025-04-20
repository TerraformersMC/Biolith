
package com.terraformersmc.biolith.impl.mixin;

import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.SaveProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value= SaveLoader.class, priority = 500)
public class MixinSaveLoader {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Record;<init>()V", shift = At.Shift.AFTER))
    private void biolith$earlyCaptureRegistries(LifecycledResourceManager lifecycledResourceManager, DataPackContents dataPackContents, CombinedDynamicRegistries<ServerDynamicRegistryType> combinedDynamicRegistries, SaveProperties saveProperties, CallbackInfo ci) {
        // Capture the registries ridiculously early on Fabric because BClib does,
        // and immediately forces us to provide biome entries...
        BiomeCoordinator.setRegistryManager(combinedDynamicRegistries);
    }
}
