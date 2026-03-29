package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.ModConfig;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = OcclusionCuller.class, remap = false)
public class SodiumOcclusionCullerMixin {

    private static int lastViewDistance = -1;
    private static double lastScale = -1.0;
    private static double cachedA2 = 0;
    private static double cachedB2 = 0;
    private static double cachedAB2 = 0;

    @Inject(method = "isWithinRenderDistance", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onIsWithinRenderDistance(CameraTransform camera, RenderSection section, float searchDistance, CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        ModConfig config = ModConfig.getInstance();

        int viewDistance = client.options.renderDistance().get();
        double scale = config.renderRadiusScale;

        if (viewDistance != lastViewDistance || scale != lastScale) {
            lastViewDistance = viewDistance;
            lastScale = scale;
            double maxRadius = viewDistance * 16.0;
            cachedA2 = maxRadius * maxRadius;
            double shortRadius = maxRadius * scale;
            cachedB2 = shortRadius * shortRadius;
            cachedAB2 = cachedA2 * cachedB2;
        }

        int originX = section.getOriginX();
        int originZ = section.getOriginZ();
        double centerX = originX + 8;
        double centerZ = originZ + 8;
        double dx = centerX - player.getX();
        double dz = centerZ - player.getZ();

        double yawRad = player.getYRot() * (Math.PI / 180.0);
        double dirX = -Mth.sin((float) yawRad);
        double dirZ = Mth.cos((float) yawRad);

        double forward = dx * dirX + dz * dirZ;
        double right   = -dx * dirZ + dz * dirX;

        if (forward * forward * cachedB2 + right * right * cachedA2 > cachedAB2) {
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