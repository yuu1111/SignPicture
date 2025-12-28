package net.teamfruit.signpic.entry;

import net.teamfruit.signpic.config.SignPicConfig;
import org.jetbrains.annotations.NotNull;

/**
 * エントリのライフサイクルを管理するスロット。
 * ティックベースのガベージコレクションに使用される。
 *
 * @param <T> 保持するエントリの型
 */
public class EntrySlot<T> {
    /** グローバルティックカウンター (全インスタンス共有) */
    protected static long times = 0;

    /** 保持するエントリ */
    protected final @NotNull T entry;

    /** 最終アクセス時刻 (ティック) */
    private long time = 0;

    /**
     * 新しいスロットを作成する。
     *
     * @param entry 保持するエントリ
     */
    public EntrySlot(final @NotNull T entry) {
        this.entry = entry;
        used();
    }

    /**
     * エントリを取得し、アクセス時刻を更新する。
     *
     * @return 保持しているエントリ
     */
    public @NotNull T get() {
        used();
        return this.entry;
    }

    /**
     * アクセス時刻を現在のティックに更新する。
     *
     * @return このスロット
     */
    public @NotNull EntrySlot<T> used() {
        this.time = times;
        return this;
    }

    /**
     * このスロットを廃棄すべきかどうかを判定する。
     *
     * @return 廃棄すべき場合true
     */
    public boolean shouldCollect() {
        return times - this.time > getCollectTimes();
    }

    /**
     * グローバルティックを進める。
     * ティック毎に呼び出される。
     */
    public static void Tick() {
        times++;
    }

    /**
     * 廃棄までのティック数を取得する。
     *
     * @return 廃棄ティック数
     */
    protected int getCollectTimes() {
        return SignPicConfig.get().entryGcDelayTicks;
    }
}
