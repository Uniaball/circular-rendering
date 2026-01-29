package com.uniaball.circularrendering.render;

import com.uniaball.circularrendering.config.CircularRenderingConfig;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;

public class CircularRenderDistance {
    
    public static boolean shouldRenderChunk(ChunkPos chunkPos, Vec3i cameraChunk, int renderDistance) {
        CircularRenderingConfig config = CircularRenderingConfig.getInstance();
        
        if (!config.enabled) {
            return isChunkInSquareRadius(chunkPos, cameraChunk, renderDistance);
        }
        
        if (config.use3DCircle) {
            return isChunkInSphericalRadius(chunkPos, cameraChunk, renderDistance, config);
        } else {
            return isChunkInCircularRadius(chunkPos, cameraChunk, renderDistance, config);
        }
    }
    
    private static boolean isChunkInSquareRadius(ChunkPos chunkPos, Vec3i cameraChunk, int renderDistance) {
        int dx = Math.abs(chunkPos.x - cameraChunk.getX());
        int dz = Math.abs(chunkPos.z - cameraChunk.getZ());
        return dx <= renderDistance && dz <= renderDistance;
    }
    
    private static boolean isChunkInCircularRadius(ChunkPos chunkPos, Vec3i cameraChunk, int renderDistance, CircularRenderingConfig config) {
        int dx = chunkPos.x - cameraChunk.getX();
        int dz = chunkPos.z - cameraChunk.getZ();
        
        double distanceSq = dx * dx + dz * dz;
        double adjustedRadius = renderDistance * config.radiusMultiplier;
        
        return distanceSq <= adjustedRadius * adjustedRadius;
    }
    
    private static boolean isChunkInSphericalRadius(ChunkPos chunkPos, Vec3i cameraChunk, int renderDistance, CircularRenderingConfig config) {
        int dx = chunkPos.x - cameraChunk.getX();
        int dz = chunkPos.z - cameraChunk.getZ();
        
        double horizontalDistanceSq = dx * dx + dz * dz;
        double adjustedRadius = renderDistance * config.radiusMultiplier;
        
        if (!config.verticalCulling) {
            return horizontalDistanceSq <= adjustedRadius * adjustedRadius;
        }
        
        // 如果需要垂直剔除，还需要检查垂直距离
        // 注意：这里我们假设相机在y=0的位置，实际实现中需要获取相机的高度
        return horizontalDistanceSq <= adjustedRadius * adjustedRadius;
    }
    
    public static double getChunkDistanceSquared(ChunkPos chunkPos, Vec3i cameraChunk) {
        int dx = chunkPos.x - cameraChunk.getX();
        int dz = chunkPos.z - cameraChunk.getZ();
        return dx * dx + dz * dz;
    }
}
