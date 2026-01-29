package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.CircularRenderingConfig;
import com.uniaball.circularrendering.render.CircularRenderDistance;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {
    
    @Inject(method = "isWithinRenderDistance", at = @At("HEAD"), cancellable = true)
    private void onIsWithinRenderDistance(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        CircularRenderingConfig config = CircularRenderingConfig.getInstance();
        
        if (config.enabled) {
            //TODO
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
