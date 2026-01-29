package com.uniaball.circularrendering.render;

import com.uniaball.circularrendering.config.CircularRenderingConfig;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkCuller {
    private static final Set<Long> cachedVisibleChunks = ConcurrentHashMap.newKeySet();
    private static Vec3i lastCameraChunk = null;
    private static int lastRenderDistance = -1;
    private static boolean lastEnabled = false;
    
    public static boolean isChunkVisible(ChunkPos chunkPos, Vec3i cameraChunk, int renderDistance) {
        CircularRenderingConfig config = CircularRenderingConfig.getInstance();
        
        if (shouldRecalculateCache(cameraChunk, renderDistance, config.enabled)) {
            recalculateCache(cameraChunk, renderDistance, config);
        }
        
        long chunkKey = ChunkPos.toLong(chunkPos.x, chunkPos.z);
        return cachedVisibleChunks.contains(chunkKey);
    }
    
    private static boolean shouldRecalculateCache(Vec3i cameraChunk, int renderDistance, boolean enabled) {
        return !cameraChunk.equals(lastCameraChunk) || 
               renderDistance != lastRenderDistance || 
               enabled != lastEnabled;
    }
    
    private static void recalculateCache(Vec3i cameraChunk, int renderDistance, CircularRenderingConfig config) {
        cachedVisibleChunks.clear();
        
        int radius = (int) Math.ceil(renderDistance * config.radiusMultiplier);
        int radiusSq = radius * radius;
        
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distanceSq = dx * dx + dz * dz;
                
                if (distanceSq <= radiusSq) {
                    int chunkX = cameraChunk.getX() + dx;
                    int chunkZ = cameraChunk.getZ() + dz;
                    long chunkKey = ChunkPos.toLong(chunkX, chunkZ);
                    cachedVisibleChunks.add(chunkKey);
                }
            }
        }
        
        lastCameraChunk = cameraChunk;
        lastRenderDistance = renderDistance;
        lastEnabled = config.enabled;
    }
    
    public static void resetCache() {
        cachedVisibleChunks.clear();
        lastCameraChunk = null;
        lastRenderDistance = -1;
        lastEnabled = false;
    }
}
