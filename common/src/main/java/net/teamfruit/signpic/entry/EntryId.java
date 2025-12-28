package net.teamfruit.signpic.entry;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a parsed sign picture entry ID.
 * Format: URL[#properties]
 */
public class EntryId {
    // Pattern to match SignPicture formatted text
    private static final Pattern SIGNPIC_PATTERN = Pattern.compile(
            "^\\s*(?<url>https?://[^#\\s]+)(?:#(?<props>.*))?\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    private final String raw;
    private final String url;
    private final String properties;
    private final boolean valid;

    private EntryId(String raw, @Nullable String url, @Nullable String properties) {
        this.raw = raw;
        this.url = url;
        this.properties = properties;
        this.valid = url != null && !url.isEmpty();
    }

    public static EntryId parse(String text) {
        if (text == null || text.isEmpty()) {
            return new EntryId("", null, null);
        }

        Matcher matcher = SIGNPIC_PATTERN.matcher(text);
        if (matcher.matches()) {
            String url = matcher.group("url");
            String props = matcher.group("props");
            return new EntryId(text, url, props);
        }

        return new EntryId(text, null, null);
    }

    /**
     * Creates an EntryId from sign text lines.
     */
    public static EntryId fromSignLines(String[] lines) {
        StringBuilder combined = new StringBuilder();
        for (String line : lines) {
            if (line != null) {
                combined.append(line);
            }
        }
        return parse(combined.toString().trim());
    }

    /**
     * Creates an EntryId from sign text (may contain newlines).
     * Returns null if the text is not a valid SignPicture URL.
     */
    @Nullable
    public static EntryId fromSignText(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        // Remove newlines and combine
        String combined = text.replace("\n", "").replace("\r", "").trim();
        EntryId id = parse(combined);
        return id.isValid() ? id : null;
    }

    public String getRaw() {
        return raw;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getProperties() {
        return properties;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * Gets a cache-safe ID string (URL without properties).
     */
    public String getCacheId() {
        return url != null ? url : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntryId entryId = (EntryId) o;
        return Objects.equals(raw, entryId.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw);
    }

    @Override
    public String toString() {
        return raw;
    }
}
