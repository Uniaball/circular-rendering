package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.ModConfig;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OcclusionCuller.class, remap = false)
public class SodiumOcclusionCullerMixin {

    @Inject(method = "isWithinRenderDistance", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsWithinRenderDistance(CameraTransform camera, RenderSection section, float searchDistance, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        ModConfig config = ModConfig.getInstance();

        int viewDistance = client.options.renderDistance().get();
        double maxRadius = viewDistance * 16.0;
        double scale = config.renderRadiusScale;
        double shortRadius = maxRadius * scale;

        double a2 = maxRadius * maxRadius;
        double b2 = shortRadius * shortRadius;
        double ab2 = a2 * b2;

        int originX = section.getOriginX();
        int originZ = section.getOriginZ();
        double centerX = originX + 8;
        double centerZ = originZ + 8;
        double dx = centerX - player.getX();
        double dz = centerZ - player.getZ();

        double yawRad = player.getYRot() * (Math.PI / 180.0);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        double forward = dx * dirX + dz * dirZ;
        double right   = -dx * dirZ + dz * dirX;

        if (forward * forward * b2 + right * right * a2 > ab2) {
            cir.setReturnValue(false);
            return;
        }

        if (config.enableVerticalRange) {
            int originY = section.getOriginY();
            int chunkY = originY >> 4;
            int playerChunkY = player.getBlockY() >> 4;
            if (Math.abs(chunkY - playerChunkY) > config.verticalRange) {
                cir.setReturnValue(false);
            }
        }
    }
}