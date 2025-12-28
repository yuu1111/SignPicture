package net.teamfruit.signpic.content;

import net.teamfruit.signpic.state.State;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Represents downloaded content (image data) that can be shared across multiple entries.
 */
public class Content {
    private final String url;
    private final State state;
    private long lastAccessTime;
    private Path cachedFile;
    private ContentTexture texture;
    private int referenceCount = 0;

    public Content(String url) {
        this.url = url;
        this.state = new State();
        this.lastAccessTime = System.currentTimeMillis();
    }

    public String getUrl() {
        return url;
    }

    public State getState() {
        return state;
    }

    @Nullable
    public Path getCachedFile() {
        return cachedFile;
    }

    public void setCachedFile(Path cachedFile) {
        this.cachedFile = cachedFile;
    }

    @Nullable
    public ContentTexture getTexture() {
        return texture;
    }

    public void setTexture(ContentTexture texture) {
        this.texture = texture;
    }

    public void touch() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void addReference() {
        referenceCount++;
    }

    public void removeReference() {
        referenceCount--;
    }

    public int getReferenceCount() {
        return referenceCount;
    }

    public boolean shouldCollect(long gcDelayMs) {
        if (referenceCount > 0) {
            return false;
        }
        return System.currentTimeMillis() - lastAccessTime > gcDelayMs;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
