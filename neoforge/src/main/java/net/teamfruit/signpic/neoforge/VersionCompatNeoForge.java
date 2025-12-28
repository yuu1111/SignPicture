package net.teamfruit.signpic.neoforge;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.teamfruit.signpic.render.VersionCompat;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * NeoForge implementation of version compatibility layer.
 * NeoForge is used for MC 1.21+ only.
 */
public class VersionCompatNeoForge implements VersionCompat.VersionCompatImpl {

    @Override
    public ResourceLocation createResourceLocation(String namespace, String path) {
        // NeoForge 1.21+ uses the factory method
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public void addVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normalMatrix,
            float x, float y, float z,
            float u, float v,
            float nx, float ny, float nz,
            int packedLight,
            int overlay,
            int r, int g, int b, int a
    ) {
        // NeoForge 1.21+ uses the addVertex + setters pattern (no endVertex)
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(packedLight)
                .setNormal(nx, ny, nz);
    }
}
