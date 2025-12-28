package net.teamfruit.signpic.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.resources.ResourceLocation;
import net.teamfruit.signpic.SignPicture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Version compatibility layer for Minecraft API differences.
 * This class provides abstraction over APIs that changed between MC versions.
 */
public class VersionCompat {
    private static VersionCompatImpl impl;

    public static void init(VersionCompatImpl implementation) {
        impl = implementation;
    }

    public static ResourceLocation createResourceLocation(String namespace, String path) {
        if (impl != null) {
            return impl.createResourceLocation(namespace, path);
        }
        throw new IllegalStateException("VersionCompat not initialized");
    }

    public static ResourceLocation parseResourceLocation(String location) {
        if (impl != null) {
            return impl.parseResourceLocation(location);
        }
        throw new IllegalStateException("VersionCompat not initialized");
    }

    public static ResourceLocation createModResourceLocation(String path) {
        return createResourceLocation(SignPicture.MOD_ID, path);
    }

    public static void addVertex(
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
        if (impl != null) {
            impl.addVertex(consumer, matrix, normalMatrix, x, y, z, u, v, nx, ny, nz, packedLight, overlay, r, g, b, a);
        }
    }

    public interface VersionCompatImpl {
        ResourceLocation createResourceLocation(String namespace, String path);
        ResourceLocation parseResourceLocation(String location);

        void addVertex(
                VertexConsumer consumer,
                Matrix4f matrix,
                Matrix3f normalMatrix,
                float x, float y, float z,
                float u, float v,
                float nx, float ny, float nz,
                int packedLight,
                int overlay,
                int r, int g, int b, int a
        );
    }
}
