package net.teamfruit.signpic.fabric;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;
import net.teamfruit.signpic.render.VersionCompat;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Fabric 1.20.x implementation of version compatibility layer.
 */
public class VersionCompatFabric implements VersionCompat.VersionCompatImpl {

    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation createResourceLocation(String namespace, String path) {
        // 1.20.x uses the constructor (deprecated in 1.21+)
        return new ResourceLocation(namespace, path);
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
        // 1.20.x uses vertex + endVertex pattern
        consumer.vertex(matrix, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(packedLight)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
    }
}
