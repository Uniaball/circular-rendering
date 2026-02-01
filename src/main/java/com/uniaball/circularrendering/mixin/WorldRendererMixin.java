package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.CircularRenderingConfig;
import com.uniaball.circularrendering.render.ChunkCuller;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = {
            "isChunkInRenderDistance(Lnet/minecraft/class_1923;Lnet/minecraft/class_2382;I)Z",
            "isChunkInRenderDistance"
    }, at = @At("HEAD"), cancellable = true)
    private void onIsChunkInRenderDistance(ChunkPos chunkPos, Vec3i cameraChunk, int renderDistance,
                                           CallbackInfoReturnable<Boolean> cir) {
        CircularRenderingConfig config = CircularRenderingConfig.getInstance();

        if (config.enabled) {
            boolean shouldRender = ChunkCuller.isChunkVisible(chunkPos, cameraChunk, renderDistance);

            if (!shouldRender) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}