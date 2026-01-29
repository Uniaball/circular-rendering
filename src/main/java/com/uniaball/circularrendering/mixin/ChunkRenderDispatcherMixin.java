package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.CircularRenderingConfig;
import com.uniaball.circularrendering.render.ChunkCuller;
import net.minecraft.client.render.chunk.ChunkRenderDispatcher;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderDispatcher.class)
public class ChunkRenderDispatcherMixin {
    
    @Inject(method = "shouldSkipCulling", at = @At("HEAD"), cancellable = true)
    private void onShouldSkipCulling(ChunkPos chunkPos, Vec3i cameraChunk, int renderDistance, 
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
