package com.terraformersmc.biolith.impl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.biolith.impl.biome.BiomeCoordinator;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
    @WrapOperation(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerChunkManager;getStructurePlacementCalculator()Lnet/minecraft/world/gen/chunk/placement/StructurePlacementCalculator;",
            ordinal = 0
    ))
    @SuppressWarnings("unused")
    private StructurePlacementCalculator biolith$serverWorldStarting(ServerChunkManager instance, Operation<StructurePlacementCalculator> original) {
        BiomeCoordinator.handleWorldStarting((ServerWorld)(Object) this);

        return original.call(instance);
    }
}
