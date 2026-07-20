package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.ModConfig;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.util.collections.WriteQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = OcclusionCuller.class, remap = false)
public abstract class SodiumOcclusionCullerMixin {

    @Shadow
    private float searchDistanceRegular;

    @Unique
    private int lastViewDistance = -1;
    @Unique
    private double lastScale = -1.0;
    @Unique
    private double cachedA2, cachedB2, cachedAB2;

    @Unique
    private LocalPlayer framePlayer;
    @Unique
    private double framePlayerX, framePlayerZ;
    @Unique
    private double frameDirX, frameDirZ;
    @Unique
    private int framePlayerChunkY;
    @Unique
    private boolean frameEnableVertical;
    @Unique
    private int frameVerticalRange;
    @Unique
    private boolean frameCachesValid;

    @Unique
    private RenderSection currentSection;

    @Inject(method = "findVisible", at = @At("HEAD"))
    private void captureFrameConstants(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            frameCachesValid = false;
            return;
        }

        int viewDistance = client.options.renderDistance().get();
        double scale = ModConfig.getInstance().renderRadiusScale;
        if (viewDistance != lastViewDistance || scale != lastScale) {
            lastViewDistance = viewDistance;
            lastScale = scale;
            double maxRadius = viewDistance * 16.0;
            double shortRadius = maxRadius * scale;
            cachedA2 = maxRadius * maxRadius;
            cachedB2 = shortRadius * shortRadius;
            cachedAB2 = cachedA2 * cachedB2;
        }

        framePlayer = player;
        framePlayerX = player.getX();
        framePlayerZ = player.getZ();
        double yawRad = player.getYRot() * (Math.PI / 180.0);
        frameDirX = -Mth.sin((float) yawRad);
        frameDirZ = Mth.cos((float) yawRad);
        framePlayerChunkY = player.getBlockY() >> 4;

        ModConfig config = ModConfig.getInstance();
        frameEnableVertical = config.enableVerticalRange;
        frameVerticalRange = config.verticalRange;
        frameCachesValid = true;
    }

    @Inject(
        method = "visitNode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/occlusion/OcclusionCuller;testDistance(FFF)Z",
            shift = At.Shift.BEFORE
        )
    )
    private void beforeTestDistance(WriteQueue<RenderSection> queue, RenderSection section,
                                     int outgoingDirection, boolean hasLocalPath,
                                     boolean hasRegularPath, boolean hasWidePath, CallbackInfo ci) {
        this.currentSection = section;
    }

    @Redirect(
        method = "visitNode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/occlusion/OcclusionCuller;testDistance(FFF)Z"
        )
    )
    private boolean redirectTestDistance(float xzThreshold, float yThreshold, float maxDistance) {
        if (!frameCachesValid || maxDistance != searchDistanceRegular || currentSection == null) {
            return (xzThreshold < (maxDistance * maxDistance)) && (yThreshold < maxDistance);
        }

        double dx = (currentSection.getOriginX() + 8.0) - framePlayerX;
        double dz = (currentSection.getOriginZ() + 8.0) - framePlayerZ;
        double forward = dx * frameDirX + dz * frameDirZ;
        double right = -dx * frameDirZ + dz * frameDirX;

        if (forward * forward * cachedB2 + right * right * cachedA2 > cachedAB2) {
            return false;
        }

        if (frameEnableVertical) {
            int chunkY = currentSection.getOriginY() >> 4;
            if (Math.abs(chunkY - framePlayerChunkY) > frameVerticalRange) {
                return false;
            }
        }

        return yThreshold < maxDistance;
    }
}