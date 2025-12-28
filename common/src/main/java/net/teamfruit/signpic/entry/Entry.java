package net.teamfruit.signpic.entry;

import net.teamfruit.signpic.attr.SignPicProperties;
import net.teamfruit.signpic.content.Content;
import net.teamfruit.signpic.content.ContentManager;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a sign picture entry with its parsed properties and content reference.
 */
public class Entry {
    private final EntryId id;
    private final SignPicProperties properties;
    private Content content;
    private long lastAccessTime;

    public Entry(EntryId id) {
        this.id = id;
        this.properties = SignPicProperties.parse(id.getProperties());
        this.lastAccessTime = System.currentTimeMillis();

        // Get or create content for this entry's URL
        if (id.isValid()) {
            this.content = ContentManager.getInstance().getOrCreate(id.getUrl());
        }
    }

    public EntryId getId() {
        return id;
    }

    public SignPicProperties getProperties() {
        return properties;
    }

    @Nullable
    public Content getContent() {
        return content;
    }

    public void touch() {
        this.lastAccessTime = System.currentTimeMillis();
        if (content != null) {
            content.touch();
        }
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public boolean isValid() {
        return id.isValid();
    }

    public boolean isLoaded() {
        return content != null && content.getState().isComplete();
    }

    public boolean isLoading() {
        return content != null && content.getState().isLoading();
    }

    public boolean isFailed() {
        return content != null && content.getState().isFailed();
    }
}
