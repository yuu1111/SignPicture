package net.teamfruit.signpic.entry;

import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.config.SignPicConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 看板画像エントリを管理するクラス。
 * エントリのライフサイクル管理とGCを担当する。
 */
public class EntryManager {
    /** シングルトンインスタンス */
    public static @NotNull EntryManager instance = new EntryManager();

    /**
     * シングルトンインスタンスを取得する。
     *
     * @return EntryManagerインスタンス
     */
    public static @NotNull EntryManager getInstance() {
        return instance;
    }

    /** エントリレジストリ (EntryId -> EntrySlot) */
    private final @NotNull Map<EntryId, EntrySlot<Entry>> registry = new ConcurrentHashMap<>();

    private EntryManager() {}

    /**
     * EntryIdに対応するエントリを取得する。
     * 存在しない場合は作成する。
     *
     * @param id EntryId
     * @return Entry
     */
    public @NotNull Entry get(final @NotNull EntryId id) {
        final EntrySlot<Entry> slot = this.registry.get(id);
        if (slot != null) {
            return slot.get();
        } else {
            final Entry entry = new Entry(id);
            this.registry.put(id, new EntrySlot<>(entry));
            return entry;
        }
    }

    /**
     * 看板テキストに対応するエントリを取得または作成する。
     *
     * @param signText 看板テキスト
     * @return エントリ
     */
    public @NotNull Entry getOrCreate(final @NotNull String signText) {
        EntryId id = EntryId.parse(signText);
        return get(id);
    }

    /**
     * EntryIdに対応するエントリを取得または作成する。
     *
     * @param entryId エントリID
     * @return エントリ、無効なIDの場合はnull
     */
    @Nullable
    public Entry getOrCreate(final @Nullable EntryId entryId) {
        if (entryId == null || !entryId.isValid()) {
            return null;
        }
        return get(entryId);
    }

    /**
     * 看板テキスト行に対応するエントリを取得または作成する。
     *
     * @param signLines 看板テキスト行の配列
     * @return エントリ
     */
    public @NotNull Entry getOrCreate(final @NotNull String[] signLines) {
        StringBuilder combined = new StringBuilder();
        for (String line : signLines) {
            if (line != null) {
                combined.append(line);
            }
        }
        return getOrCreate(combined.toString().trim());
    }

    /**
     * ティック処理。
     * GCを実行する。
     */
    public void onTick() {
        // グローバルティック更新
        EntrySlot.Tick();

        // GC処理
        for (final Iterator<Map.Entry<EntryId, EntrySlot<Entry>>> itr = this.registry.entrySet().iterator(); itr.hasNext();) {
            final Map.Entry<EntryId, EntrySlot<Entry>> mapEntry = itr.next();
            final EntrySlot<Entry> slot = mapEntry.getValue();

            if (slot.shouldCollect()) {
                slot.get().onCollect();
                SignPicture.LOGGER.debug("GC: エントリを削除 {}", mapEntry.getKey());
                itr.remove();
            }
        }
    }

    /**
     * 未使用エントリのガベージコレクションを実行する (onTick()へのエイリアス)。
     */
    public void gc() {
        onTick();
    }

    /**
     * すべてのエントリを削除する。
     */
    public void clear() {
        for (EntrySlot<Entry> slot : registry.values()) {
            slot.get().onCollect();
        }
        registry.clear();
    }

    /**
     * 登録されているエントリ数を取得する。
     *
     * @return エントリ数
     */
    public int getEntryCount() {
        return registry.size();
    }
}
