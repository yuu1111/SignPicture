package net.teamfruit.signpic.entry;

import net.teamfruit.signpic.attr.SignPicProperties;
import net.teamfruit.signpic.content.Content;
import net.teamfruit.signpic.content.ContentId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 看板画像エントリを表すクラス。
 * パースされたプロパティとコンテンツへの参照を保持する。
 */
public class Entry implements ICollectable {
    /** エントリID */
    private final @NotNull EntryId id;

    /** パースされたプロパティ */
    private final @NotNull SignPicProperties properties;

    /** 画像コンテンツへの参照 */
    private @Nullable Content content;

    /** GC対象フラグ */
    private boolean collected = false;

    /**
     * エントリを作成する。
     *
     * @param id エントリID
     */
    public Entry(final @NotNull EntryId id) {
        this.id = id;
        this.properties = SignPicProperties.parse(id.getProperties());

        // ContentIdに対応するコンテンツを取得または作成
        ContentId contentId = id.getContentId();
        if (contentId != null) {
            this.content = contentId.content();
        }
    }

    /**
     * エントリIDを取得する。
     *
     * @return エントリID
     */
    public @NotNull EntryId getId() {
        return id;
    }

    /**
     * プロパティを取得する。
     *
     * @return プロパティ
     */
    public @NotNull SignPicProperties getProperties() {
        return properties;
    }

    /**
     * コンテンツを取得する。
     *
     * @return コンテンツ、無効な場合はnull
     */
    @Nullable
    public Content getContent() {
        return content;
    }

    /**
     * アクセス時刻を更新する。
     * GCによる削除を防ぐために使用。
     */
    public void touch() {
        if (content != null) {
            content.touch();
        }
    }

    /**
     * エントリが有効かどうかを判定する。
     *
     * @return 有効なURLを持つ場合true
     */
    public boolean isValid() {
        return id.isValid();
    }

    /**
     * コンテンツが読み込み完了かどうかを判定する。
     *
     * @return 読み込み完了の場合true
     */
    public boolean isLoaded() {
        return content != null && content.getState().isComplete();
    }

    /**
     * コンテンツが読み込み中かどうかを判定する。
     *
     * @return 読み込み中の場合true
     */
    public boolean isLoading() {
        return content != null && content.getState().isLoading();
    }

    /**
     * コンテンツの読み込みが失敗したかどうかを判定する。
     *
     * @return エラーの場合true
     */
    public boolean isFailed() {
        return content != null && content.getState().isFailed();
    }

    /**
     * GC対象としてマークする。
     */
    public void markCollected() {
        this.collected = true;
    }

    @Override
    public void onCollect() {
        // コンテンツへの参照を解放
        this.content = null;
        this.collected = true;
    }

    @Override
    public boolean shouldCollect() {
        return this.collected;
    }
}
