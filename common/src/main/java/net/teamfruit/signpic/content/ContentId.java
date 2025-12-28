package net.teamfruit.signpic.content;

import net.minecraft.resources.ResourceLocation;
import net.teamfruit.signpic.render.VersionCompat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 画像コンテンツのIDを表すクラス。
 * URLを圧縮形式で保存する (http:// → 無印, https:// → $, リソース → !)。
 */
public class ContentId {
    /** 非表示テクスチャのリソースロケーション (遅延初期化) */
    private static ResourceLocation hideTexture;

    /** 非表示コンテンツID (遅延初期化) */
    private static ContentId hideContent;

    /**
     * 非表示テクスチャのリソースロケーションを取得する。
     *
     * @return リソースロケーション
     */
    public static @NotNull ResourceLocation getHideTexture() {
        if (hideTexture == null) {
            hideTexture = VersionCompat.createResourceLocation("signpic", "textures/state/hide.png");
        }
        return hideTexture;
    }

    /**
     * 非表示コンテンツIDを取得する。
     *
     * @return ContentId
     */
    public static @NotNull ContentId getHideContent() {
        if (hideContent == null) {
            hideContent = ContentId.fromResource(getHideTexture());
        }
        return hideContent;
    }

    /** 圧縮形式のID */
    private final @NotNull String id;

    /**
     * URIからContentIdを構築する。
     * URL形式を圧縮して保存する。
     *
     * @param uri URI文字列
     */
    protected ContentId(@NotNull String uri) {
        if (uri.contains("http://")) {
            uri = uri.substring(7);
        } else if (uri.contains("https://")) {
            uri = "$" + uri.substring(8);
        }
        this.id = uri;
    }

    /**
     * 圧縮形式のIDを取得する。
     *
     * @return 圧縮ID
     */
    public @NotNull String getID() {
        return this.id;
    }

    /**
     * 完全なURI形式を取得する。
     * 圧縮形式を展開して返す。
     *
     * @return 完全なURI
     */
    public @NotNull String getURI() {
        if (!this.id.startsWith("!")) {
            if (this.id.startsWith("$")) {
                return "https://" + this.id.substring(1);
            } else if (!this.id.startsWith("http://") && !this.id.startsWith("https://")) {
                return "http://" + this.id;
            }
        }
        return this.id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id.hashCode();
        return result;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ContentId))
            return false;
        final ContentId other = (ContentId) obj;
        return this.id.equals(other.id);
    }

    @Override
    public @NotNull String toString() {
        return String.format("ContentId [id=%s]", this.id);
    }

    /**
     * リソース画像かどうかを判定する。
     *
     * @return リソース画像の場合true
     */
    public boolean isResource() {
        return this.id.startsWith("!");
    }

    /**
     * リソースロケーションを取得する。
     *
     * @return リソースロケーション
     */
    public @NotNull ResourceLocation getResource() {
        return VersionCompat.parseResourceLocation(this.id.substring(1));
    }

    /**
     * このContentIdに対応するContentを取得する。
     *
     * @return Content
     */
    public @NotNull Content content() {
        return ContentManager.getInstance().get(this);
    }

    /**
     * URI文字列からContentIdを作成する。
     *
     * @param uri URI文字列
     * @return ContentId
     */
    public static @NotNull ContentId from(final @NotNull String uri) {
        return new ContentId(uri);
    }

    /**
     * リソースロケーションからContentIdを作成する。
     *
     * @param location リソースロケーション
     * @return ContentId
     */
    public static @NotNull ContentId fromResource(final @NotNull ResourceLocation location) {
        return from("!" + location.toString());
    }
}
