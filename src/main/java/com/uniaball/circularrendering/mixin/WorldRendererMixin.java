package com.uniaball.circularrendering.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Redirect(
        method = "updateChunks(Lnet/minecraft/client/render/Camera;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;scheduleChunkRender(IIIZ)V"
        )
    )
    private void redirectScheduleChunkRender(WorldRenderer worldRenderer, int x, int y, int z, boolean important) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            worldRenderer.scheduleChunkRender(x, y, z, important);
            return;
        }

        int viewDistance = client.options.getViewDistance().getValue();
        double radius = viewDistance * 16.0;
        double centerX = x * 16 + 8;
        double centerZ = z * 16 + 8;
        double dx = centerX - client.player.getX();
        double dz = centerZ - client.player.getZ();

        if (dx * dx + dz * dz <= radius * radius) {
            worldRenderer.scheduleChunkRender(x, y, z, important);
        }
    }
}