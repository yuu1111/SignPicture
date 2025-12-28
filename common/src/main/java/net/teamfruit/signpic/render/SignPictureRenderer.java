package net.teamfruit.signpic.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.Content;
import net.teamfruit.signpic.content.ContentTexture;
import net.teamfruit.signpic.entry.Entry;
import net.teamfruit.signpic.entry.EntryId;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.state.StateType;

/**
 * Handles rendering of SignPicture images on signs.
 */
public class SignPictureRenderer {

    private static final float SIGN_WIDTH = 1.0f;
    private static final float SIGN_HEIGHT = 0.5f;

    public static void renderSignPicture(
            SignBlockEntity sign,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        // Get entry from sign text
        String signText = getSignText(sign);
        if (signText == null || signText.isEmpty()) {
            return;
        }

        // Check if this is a SignPicture URL
        EntryId entryId = EntryId.fromSignText(signText);
        if (entryId == null) {
            return;
        }

        // Log that we detected a SignPicture sign
        SignPicture.LOGGER.debug("SignPicture URL detected: {}", entryId.getUrl());

        Entry entry = EntryManager.getInstance().getOrCreate(entryId);
        if (entry == null || !entry.isValid()) {
            return;
        }

        Content content = entry.getContent();
        if (content == null) {
            return;
        }

        // Log the current state
        StateType stateType = content.getState().getType();
        SignPicture.LOGGER.debug("Content state: {}", stateType);

        // Check if content is ready
        if (stateType != StateType.LOADED) {
            return;
        }

        ContentTexture texture = content.getTexture();
        if (texture == null) {
            return;
        }

        // Mark as accessed
        content.touch();

        // Render the image
        renderImage(sign, poseStack, bufferSource, texture, partialTick, packedLight);
    }

    private static String getSignText(SignBlockEntity sign) {
        // Combine all lines of sign text
        StringBuilder sb = new StringBuilder();
        // In 1.20+, signs have front and back text
        try {
            var frontText = sign.getFrontText();
            for (int i = 0; i < 4; i++) {
                String line = frontText.getMessage(i, false).getString();
                if (!line.isEmpty()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(line);
                }
            }
        } catch (Exception e) {
            // Fallback for older versions or errors
            SignPicture.LOGGER.debug("Failed to read sign text", e);
        }
        return sb.toString();
    }

    private static void renderImage(
            SignBlockEntity sign,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            ContentTexture texture,
            float partialTick,
            int packedLight
    ) {
        BlockState state = sign.getBlockState();

        poseStack.pushPose();

        // Position based on sign type (standing or wall)
        boolean isStanding = state.hasProperty(BlockStateProperties.ROTATION_16);
        float rotation = 0;

        if (isStanding) {
            // Standing sign - rotate based on rotation property
            int rot = state.getValue(BlockStateProperties.ROTATION_16);
            rotation = -(rot * 360f / 16f);
            poseStack.translate(0.5, 0.75, 0.5);
        } else if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            // Wall sign - rotate based on facing direction
            var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            switch (facing) {
                case NORTH -> rotation = 180;
                case SOUTH -> rotation = 0;
                case WEST -> rotation = 90;
                case EAST -> rotation = -90;
            }
            poseStack.translate(0.5, 0.75, 0.5);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0, 0, -0.4375);
        } else {
            poseStack.translate(0.5, 0.75, 0.5);
        }

        if (isStanding) {
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
        }

        // Get the current frame for animated images
        float time = (System.currentTimeMillis() % 100000) / 1000f;
        ContentTexture.Frame frame = texture.getFrameForTime(time);
        if (frame == null) {
            poseStack.popPose();
            return;
        }

        // Calculate aspect ratio preserving dimensions
        float imgWidth = texture.getWidth();
        float imgHeight = texture.getHeight();
        float aspectRatio = imgWidth / imgHeight;

        float renderWidth = SIGN_WIDTH;
        float renderHeight = SIGN_HEIGHT;

        if (aspectRatio > SIGN_WIDTH / SIGN_HEIGHT) {
            renderHeight = renderWidth / aspectRatio;
        } else {
            renderWidth = renderHeight * aspectRatio;
        }

        // Center the image
        float offsetX = -renderWidth / 2;
        float offsetY = -renderHeight / 2;

        // Render the textured quad
        RenderHelper.drawTexturedQuad(
                poseStack,
                bufferSource,
                frame.getTextureLocation(),
                offsetX, offsetY,
                renderWidth, renderHeight,
                packedLight
        );

        poseStack.popPose();
    }
}
