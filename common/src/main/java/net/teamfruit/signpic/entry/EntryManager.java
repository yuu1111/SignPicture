package net.teamfruit.signpic.entry;

import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.config.SignPicConfig;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages sign picture entries and their lifecycle.
 */
public class EntryManager {
    private static EntryManager instance;

    public static EntryManager getInstance() {
        if (instance == null) {
            instance = new EntryManager();
        }
        return instance;
    }

    private final Map<String, Entry> entryMap = new ConcurrentHashMap<>();

    private EntryManager() {}

    /**
     * Gets an entry for the given sign text, creating one if it doesn't exist.
     */
    public Entry getOrCreate(String signText) {
        return entryMap.computeIfAbsent(signText, text -> {
            EntryId id = EntryId.parse(text);
            return new Entry(id);
        });
    }

    /**
     * Gets an entry for the given EntryId, creating one if it doesn't exist.
     */
    @Nullable
    public Entry getOrCreate(EntryId entryId) {
        if (entryId == null || !entryId.isValid()) {
            return null;
        }
        return entryMap.computeIfAbsent(entryId.getRaw(), key -> new Entry(entryId));
    }

    /**
     * Gets an entry for sign lines, creating one if it doesn't exist.
     */
    public Entry getOrCreate(String[] signLines) {
        StringBuilder combined = new StringBuilder();
        for (String line : signLines) {
            if (line != null) {
                combined.append(line);
            }
        }
        return getOrCreate(combined.toString().trim());
    }

    @Nullable
    public Entry get(String signText) {
        return entryMap.get(signText);
    }

    /**
     * Runs garbage collection on unused entries.
     */
    public void gc() {
        long gcDelayMs = SignPicConfig.get().entryGcDelayTicks * 50L;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Entry>> it = entryMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, Entry> mapEntry = it.next();
            Entry entry = mapEntry.getValue();

            if (now - entry.getLastAccessTime() > gcDelayMs) {
                SignPicture.LOGGER.debug("GC: Removing entry {}", mapEntry.getKey());
                it.remove();
            }
        }
    }

    /**
     * Clears all entries.
     */
    public void clear() {
        entryMap.clear();
    }

    public int getEntryCount() {
        return entryMap.size();
    }
}
