package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.ModConfig;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OcclusionCuller.class, remap = false)
public class SodiumOcclusionCullerMixin {

    @Inject(method = "isWithinRenderDistance", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsWithinRenderDistance(CameraTransform camera, RenderSection section, float searchDistance, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        int viewDistance = client.options.getViewDistance().getValue();
        double baseRadius = viewDistance * 16.0;
        double scale = ModConfig.getInstance().renderRadiusScale;
        double radius = baseRadius * scale;
        double radiusSq = radius * radius;

        int originX = section.getOriginX();
        int originZ = section.getOriginZ();
        double centerX = originX + 8;
        double centerZ = originZ + 8;
        double dx = centerX - player.getX();
        double dz = centerZ - player.getZ();

        if (dx * dx + dz * dz > radiusSq) {
            cir.setReturnValue(false);
        }
    }
}