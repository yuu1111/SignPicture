package net.teamfruit.signpic.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Helper class for rendering textured quads.
 * Uses RenderType for version-compatible rendering.
 */
public class RenderHelper {

    /**
     * Draws a textured quad at the specified position.
     *
     * @param poseStack     The pose stack for transformations
     * @param bufferSource  The buffer source for rendering
     * @param texture       The texture ResourceLocation
     * @param x             X offset from origin
     * @param y             Y offset from origin
     * @param width         Width of the quad
     * @param height        Height of the quad
     * @param packedLight   Packed light value
     */
    public static void drawTexturedQuad(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ResourceLocation texture,
            float x,
            float y,
            float width,
            float height,
            int packedLight
    ) {
        if (texture == null) {
            return;
        }

        // Use entityCutout RenderType for textured rendering with transparency
        RenderType renderType = RenderType.entityCutout(texture);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float z1 = 0.0625f;  // Slightly in front of sign face
        float z2 = -0.0625f; // Slightly behind sign face

        // Full brightness white color
        int r = 255, g = 255, b = 255, a = 255;

        // Front face (normal pointing +Z)
        addVertex(consumer, matrix, normalMatrix, x, y, z1, 0, 1, 0, 0, 1, packedLight, r, g, b, a);
        addVertex(consumer, matrix, normalMatrix, x + width, y, z1, 1, 1, 0, 0, 1, packedLight, r, g, b, a);
        addVertex(consumer, matrix, normalMatrix, x + width, y + height, z1, 1, 0, 0, 0, 1, packedLight, r, g, b, a);
        addVertex(consumer, matrix, normalMatrix, x, y + height, z1, 0, 0, 0, 0, 1, packedLight, r, g, b, a);

        // Back face (normal pointing -Z)
        addVertex(consumer, matrix, normalMatrix, x, y + height, z2, 0, 0, 0, 0, -1, packedLight, r, g, b, a);
        addVertex(consumer, matrix, normalMatrix, x + width, y + height, z2, 1, 0, 0, 0, -1, packedLight, r, g, b, a);
        addVertex(consumer, matrix, normalMatrix, x + width, y, z2, 1, 1, 0, 0, -1, packedLight, r, g, b, a);
        addVertex(consumer, matrix, normalMatrix, x, y, z2, 0, 1, 0, 0, -1, packedLight, r, g, b, a);
    }

    private static void addVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Matrix3f normalMatrix,
            float x, float y, float z,
            float u, float v,
            float nx, float ny, float nz,
            int packedLight,
            int r, int g, int b, int a
    ) {
        VersionCompat.addVertex(consumer, matrix, normalMatrix, x, y, z, u, v, nx, ny, nz,
                packedLight, OverlayTexture.NO_OVERLAY, r, g, b, a);
    }
}
