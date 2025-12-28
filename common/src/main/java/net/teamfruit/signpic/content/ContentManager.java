package net.teamfruit.signpic.content;

import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.config.SignPicConfig;
import net.teamfruit.signpic.http.Communicator;
import net.teamfruit.signpic.http.ContentDownloader;
import net.teamfruit.signpic.state.StateType;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages content loading, caching, and garbage collection.
 */
public class ContentManager {
    private static ContentManager instance;
    private static Path cacheDirectory;

    public static ContentManager getInstance() {
        if (instance == null) {
            instance = new ContentManager();
        }
        return instance;
    }

    public static void setCacheDirectory(Path directory) {
        cacheDirectory = directory;
    }

    public static Path getCacheDirectory() {
        return cacheDirectory;
    }

    private final Map<String, Content> contentMap = new ConcurrentHashMap<>();

    private ContentManager() {}

    /**
     * Gets existing content or creates a new one for the given URL.
     */
    public Content getOrCreate(String url) {
        return contentMap.computeIfAbsent(url, this::createContent);
    }

    @Nullable
    public Content get(String url) {
        return contentMap.get(url);
    }

    private Content createContent(String url) {
        Content content = new Content(url);
        scheduleLoad(content);
        return content;
    }

    private void scheduleLoad(Content content) {
        content.getState().setType(StateType.WAITING);

        Path cacheFile = getCacheFile(content.getUrl());
        content.setCachedFile(cacheFile);

        // Check if already cached
        if (cacheFile != null && cacheFile.toFile().exists()) {
            content.getState().setType(StateType.LOADING);
            // TODO: Load from cache
            SignPicture.LOGGER.debug("Content found in cache: {}", cacheFile);
        } else if (cacheFile != null) {
            // Download content
            ContentDownloader downloader = new ContentDownloader(
                    content.getUrl(),
                    cacheFile,
                    content.getState()
            );
            Communicator.getInstance().submit(downloader);
        }
    }

    @Nullable
    private Path getCacheFile(String url) {
        if (cacheDirectory == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(url.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // Get file extension from URL
            String extension = ".cache";
            int lastDot = url.lastIndexOf('.');
            int lastSlash = url.lastIndexOf('/');
            if (lastDot > lastSlash && lastDot < url.length() - 1) {
                String ext = url.substring(lastDot);
                if (ext.length() <= 5 && ext.matches("\\.[a-zA-Z0-9]+")) {
                    extension = ext.toLowerCase();
                }
            }

            return cacheDirectory.resolve(hexString + extension);
        } catch (Exception e) {
            SignPicture.LOGGER.error("Failed to generate cache path", e);
            return null;
        }
    }

    /**
     * Runs garbage collection on unused content.
     */
    public void gc() {
        long gcDelayMs = SignPicConfig.get().contentGcDelayTicks * 50L;
        Iterator<Map.Entry<String, Content>> it = contentMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, Content> entry = it.next();
            Content content = entry.getValue();

            if (content.shouldCollect(gcDelayMs)) {
                SignPicture.LOGGER.debug("GC: Removing content {}", entry.getKey());
                content.dispose();
                it.remove();
            }
        }
    }

    /**
     * Clears all content and disposes resources.
     */
    public void clear() {
        for (Content content : contentMap.values()) {
            content.dispose();
        }
        contentMap.clear();
    }

    public int getContentCount() {
        return contentMap.size();
    }
}
