package net.teamfruit.signpic.content;

import net.teamfruit.signpic.entry.ICollectable;
import net.teamfruit.signpic.state.State;
import net.teamfruit.signpic.state.StateType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * ダウンロードされたコンテンツ (画像データ) を表すクラス。
 * 複数のエントリ間で共有可能。
 */
public class Content implements ICollectable {
    /** コンテンツID */
    public final @NotNull ContentId id;

    /** 状態 */
    public final @NotNull State state;

    /** 画像から抽出されたメタデータ */
    public @Nullable String imagemeta;

    /** キャッシュファイルパス */
    private @Nullable Path cachedFile;

    /** テクスチャ */
    private @Nullable ContentTexture texture;

    /** 廃棄フラグ */
    private boolean dirty;

    /**
     * コンテンツを作成する。
     *
     * @param id コンテンツID
     */
    public Content(final @NotNull ContentId id) {
        this.id = id;
        this.state = new State();
        this.state.setName(id.getID());
    }

    /**
     * 初期化処理。
     */
    public void onInit() {
        this.state.setType(StateType.INITALIZED);
    }

    /**
     * リソース解放処理。
     */
    @Override
    public void onCollect() {
        dispose();
    }

    /**
     * 廃棄すべきかどうかを判定する。
     *
     * @return 廃棄すべき場合true
     */
    public boolean shouldCollect() {
        return this.dirty;
    }

    /**
     * 廃棄フラグを設定する。
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * キャッシュをクリアして廃棄フラグを設定する。
     */
    public void markDirtyWithCache() {
        // TODO: キャッシュメタの更新
        markDirty();
    }

    /**
     * アクセス時刻を更新する (ContentSlotのused()に委譲)。
     * 現在の実装ではContentSlotが管理するため、このメソッドは何もしない。
     */
    public void touch() {
        // ContentSlotがアクセス時刻を管理する
    }

    /**
     * URIを取得する。
     *
     * @return 完全なURI
     */
    public @NotNull String getUrl() {
        return id.getURI();
    }

    /**
     * 状態を取得する。
     *
     * @return 状態
     */
    public @NotNull State getState() {
        return state;
    }

    /**
     * キャッシュファイルパスを取得する。
     *
     * @return キャッシュファイルパス
     */
    @Nullable
    public Path getCachedFile() {
        return cachedFile;
    }

    /**
     * キャッシュファイルパスを設定する。
     *
     * @param cachedFile キャッシュファイルパス
     */
    public void setCachedFile(@Nullable Path cachedFile) {
        this.cachedFile = cachedFile;
    }

    /**
     * テクスチャを取得する。
     *
     * @return テクスチャ
     */
    @Nullable
    public ContentTexture getTexture() {
        return texture;
    }

    /**
     * テクスチャを設定する。
     *
     * @param texture テクスチャ
     */
    public void setTexture(@Nullable ContentTexture texture) {
        this.texture = texture;
    }

    /**
     * リソースを解放する。
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
