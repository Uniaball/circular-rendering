package com.uniaball.circularrendering.mixin;

import com.uniaball.circularrendering.config.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Redirect(
        method = "renderBlockLayers",
        at = @At(
            value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/objects/ObjectArrayList;listIterator(I)Lit/unimi/dsi/fastutil/objects/ObjectListIterator;"
        )
    )
    private ObjectListIterator<ChunkBuilder.BuiltChunk> filterChunksForCircularRender(
            ObjectArrayList<ChunkBuilder.BuiltChunk> list,
            int index,
            Matrix4fc matrix,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return list.listIterator(index);
        }

        ModConfig config = ModConfig.getInstance();

        int viewDistance = client.options.getViewDistance().getValue();
        double baseRadius = viewDistance * 16.0;
        double scale = config.renderRadiusScale;
        double radius = baseRadius * scale;
        double radiusSq = radius * radius;
        double playerX = client.player.getX();
        double playerZ = client.player.getZ();

        boolean verticalEnabled = config.enableVerticalRange;
        int verticalRange = config.verticalRange;
        int playerChunkY = client.player.getBlockY() >> 4;

        List<ChunkBuilder.BuiltChunk> filtered = new ArrayList<>();
        for (ChunkBuilder.BuiltChunk chunk : list) {
            BlockPos origin = chunk.getOrigin();
            double centerX = origin.getX() + 8;
            double centerZ = origin.getZ() + 8;
            double dx = centerX - playerX;
            double dz = centerZ - playerZ;

            if (dx * dx + dz * dz > radiusSq) {
                continue;
            }

            if (verticalEnabled) {
                int chunkY = origin.getY() >> 4;
                int dy = Math.abs(chunkY - playerChunkY);
                if (dy > verticalRange) {
                    continue;
                }
            }

            filtered.add(chunk);
        }

        return ObjectArrayList.wrap(filtered.toArray(new ChunkBuilder.BuiltChunk[0])).listIterator(index);
    }
}