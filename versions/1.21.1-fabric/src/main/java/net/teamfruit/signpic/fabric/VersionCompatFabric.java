package net.teamfruit.signpic.fabric;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;
import net.teamfruit.signpic.render.VersionCompat;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Fabric 1.21.x implementation of version compatibility layer.
 */
public class VersionCompatFabric implements VersionCompat.VersionCompatImpl {

    @Override
    public ResourceLocation createResourceLocation(String namespace, String path) {
        // 1.21.x uses the factory method
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    @Override
    public ResourceLocation parseResourceLocation(String location) {
        // 1.21.x uses the parse method
        return ResourceLocation.parse(location);
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
        // 1.21.x uses addVertex + setters pattern (no endVertex needed)
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(packedLight)
                .setNormal(nx, ny, nz);
    }
}
