package net.teamfruit.signpic.content;

import net.minecraft.resources.ResourceLocation;
import net.teamfruit.signpic.image.ImageLoader;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a loaded texture that can be rendered.
 */
public class ContentTexture {
    private final List<Frame> frames;
    private final int width;
    private final int height;
    private final boolean animated;

    public ContentTexture(List<Frame> frames, int width, int height) {
        this.frames = frames;
        this.width = width;
        this.height = height;
        this.animated = frames.size() > 1;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAnimated() {
        return animated;
    }

    public int getFrameCount() {
        return frames.size();
    }

    @Nullable
    public Frame getFrame(int index) {
        if (frames.isEmpty()) return null;
        return frames.get(index % frames.size());
    }

    /**
     * Gets the appropriate frame for the given time in seconds.
     */
    @Nullable
    public Frame getFrameForTime(float timeSeconds) {
        if (frames.isEmpty()) {
            return null;
        }

        if (!animated) {
            return frames.get(0);
        }

        float totalDuration = 0;
        for (Frame frame : frames) {
            totalDuration += frame.duration;
        }

        if (totalDuration <= 0) {
            return frames.get(0);
        }

        float time = timeSeconds % totalDuration;
        float elapsed = 0;
        for (Frame frame : frames) {
            elapsed += frame.duration;
            if (time < elapsed) {
                return frame;
            }
        }

        return frames.get(frames.size() - 1);
    }

    public void dispose() {
        for (Frame frame : frames) {
            frame.dispose();
        }
        frames.clear();
    }

    /**
     * Represents a single frame of the texture (for animated images).
     */
    public static class Frame {
        private final ResourceLocation textureLocation;
        private final float duration; // Duration in seconds (0 for static images)

        public Frame(ResourceLocation textureLocation, float duration) {
            this.textureLocation = textureLocation;
            this.duration = duration;
        }

        public ResourceLocation getTextureLocation() {
            return textureLocation;
        }

        public float getDuration() {
            return duration;
        }

        public void dispose() {
            if (textureLocation != null) {
                ImageLoader.unregisterTexture(textureLocation);
            }
        }
    }
}
