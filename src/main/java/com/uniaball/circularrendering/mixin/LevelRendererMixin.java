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

    @Inject(method = "cullTerrain", at = @At("RETURN"))
    private void filterVisibleSections(Camera camera, Frustum frustum, boolean spectator, CallbackInfo ci) {
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

        double playerX = player.getX();
        double playerZ = player.getZ();
        double yawRad = player.getYRot() * (Math.PI / 180.0);
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);

        boolean verticalEnabled = config.enableVerticalRange;
        int verticalRange = config.verticalRange;
        int playerChunkY = player.getBlockY() >> 4;

        Iterator<RenderSection> iterator = visibleSections.iterator();
        while (iterator.hasNext()) {
            RenderSection section = iterator.next();
            BlockPos origin = section.getRenderOrigin();
            double centerX = origin.getX() + 8;
            double centerZ = origin.getZ() + 8;
            double dx = centerX - playerX;
            double dz = centerZ - playerZ;

            double forward = dx * dirX + dz * dirZ;
            double right   = -dx * dirZ + dz * dirX;

            if (forward * forward * b2 + right * right * a2 > ab2) {
                iterator.remove();
                continue;
            }

            if (verticalEnabled) {
                int chunkY = origin.getY() >> 4;
                if (Math.abs(chunkY - playerChunkY) > verticalRange) {
                    iterator.remove();
                }
            }
        }
    }
}