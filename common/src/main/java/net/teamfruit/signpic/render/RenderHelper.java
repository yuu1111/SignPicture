package net.teamfruit.signpic.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.teamfruit.signpic.SignPicture;

/**
 * Helper class for version-specific rendering operations.
 * TODO: Implement actual rendering for each MC version
 */
public class RenderHelper {

    /**
     * Draws a textured quad at the specified position.
     *
     * @param poseStack    The pose stack for transformations
     * @param textureId    The OpenGL texture ID to render
     * @param x            X offset from origin
     * @param y            Y offset from origin
     * @param width        Width of the quad
     * @param height       Height of the quad
     * @param packedLight  Packed light value
     */
    public static void drawTexturedQuad(
            PoseStack poseStack,
            int textureId,
            float x,
            float y,
            float width,
            float height,
            int packedLight
    ) {
        // TODO: Implement version-specific rendering
        // For now, just log that we would render
        SignPicture.LOGGER.debug("RenderHelper.drawTexturedQuad: texture={}, pos=({}, {}), size={}x{}",
                textureId, x, y, width, height);
    }
}
