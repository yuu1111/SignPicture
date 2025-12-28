package net.teamfruit.signpic.content;

import java.util.List;

/**
 * Represents a loaded texture that can be rendered.
 * This is platform-agnostic; actual OpenGL texture management is done by platform implementations.
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

    public Frame getFrame(int index) {
        return frames.get(index % frames.size());
    }

    /**
     * Gets the appropriate frame for the given time in seconds.
     */
    public Frame getFrameForTime(float timeSeconds) {
        if (!animated || frames.isEmpty()) {
            return frames.isEmpty() ? null : frames.get(0);
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
        private final int textureId;
        private final float duration; // Duration in seconds

        public Frame(int textureId, float duration) {
            this.textureId = textureId;
            this.duration = duration;
        }

        public int getTextureId() {
            return textureId;
        }

        public float getDuration() {
            return duration;
        }

        public void dispose() {
            // Platform-specific texture disposal will be handled by the renderer
        }
    }
}
