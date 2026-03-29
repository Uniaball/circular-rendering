package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    private ObjectArrayList<RenderSection> visibleSections;

    private int lastViewDistance = -1;
    private double lastScale = -1.0;
    private double cachedA2 = 0;
    private double cachedB2 = 0;
    private double cachedAB2 = 0;

    @Inject(method = "cullTerrain", at = @At("RETURN"))
    private void filterVisibleSections(Camera camera, Frustum frustum, boolean spectator, CallbackInfo ci) {
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

        double playerX = player.getX();
        double playerZ = player.getZ();
        double yawRad = player.getYRot() * (Math.PI / 180.0);
        double dirX = -Mth.sin((float) yawRad);
        double dirZ = Mth.cos((float) yawRad);

        boolean verticalEnabled = config.enableVerticalRange;
        Iterator<RenderSection> iterator = visibleSections.iterator();

        if (verticalEnabled) {
            int verticalRange = config.verticalRange;
            int playerChunkY = player.getBlockY() >> 4;

            while (iterator.hasNext()) {
                RenderSection section = iterator.next();
                BlockPos origin = section.getRenderOrigin();
                double centerX = origin.getX() + 8;
                double centerZ = origin.getZ() + 8;
                double dx = centerX - playerX;
                double dz = centerZ - playerZ;

                double forward = dx * dirX + dz * dirZ;
                double right = -dx * dirZ + dz * dirX;

                if (forward * forward * cachedB2 + right * right * cachedA2 > cachedAB2) {
                    iterator.remove();
                    continue;
                }

                int chunkY = origin.getY() >> 4;
                if (Math.abs(chunkY - playerChunkY) > verticalRange) {
                    iterator.remove();
                }
            }
        } else {
            while (iterator.hasNext()) {
                RenderSection section = iterator.next();
                BlockPos origin = section.getRenderOrigin();
                double centerX = origin.getX() + 8;
                double centerZ = origin.getZ() + 8;
                double dx = centerX - playerX;
                double dz = centerZ - playerZ;

                double forward = dx * dirX + dz * dirZ;
                double right = -dx * dirZ + dz * dirX;

                if (forward * forward * cachedB2 + right * right * cachedA2 > cachedAB2) {
                    iterator.remove();
                }
            }
        }
    }
}