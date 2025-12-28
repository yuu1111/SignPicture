package net.teamfruit.signpic.entry;

import net.teamfruit.signpic.content.ContentId;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 看板画像エントリIDを表すクラス。
 * 看板テキストから抽出されたURL+メタデータを保持する。
 */
public class EntryId {
    /** 空のエントリID */
    public static final @NotNull EntryId blank = new EntryId("");

    /** 元のID文字列 */
    private final @NotNull String id;

    /**
     * エントリIDを構築する。
     *
     * @param id ID文字列
     */
    protected EntryId(final @NotNull String id) {
        this.id = id;
    }

    /**
     * ID文字列を取得する。
     *
     * @return ID文字列
     */
    public @NotNull String id() {
        return this.id;
    }

    /**
     * 元のテキストを取得する (id()のエイリアス)。
     *
     * @return 元のテキスト
     */
    public @NotNull String getRaw() {
        return this.id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id().hashCode();
        return result;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof EntryId))
            return false;
        final EntryId other = (EntryId) obj;
        return id().equals(other.id());
    }

    @Override
    public @NotNull String toString() {
        return String.format("EntryId [id=%s]", id());
    }

    /**
     * 文字列からEntryIdを作成する。
     *
     * @param string 文字列
     * @return EntryId
     */
    public static @NotNull EntryId from(final @Nullable String string) {
        if (string != null && !StringUtils.isEmpty(string)) {
            if (StringUtils.equals(string, PreviewEntryId.PREVIEW_ID)) {
                return PreviewEntryId.instance;
            } else {
                return new EntryId(string);
            }
        }
        return blank;
    }

    /**
     * テキストからエントリIDをパースする。
     *
     * @param text パースするテキスト
     * @return パースされたエントリID
     */
    public static @NotNull EntryId parse(final @Nullable String text) {
        return from(text);
    }

    /**
     * 看板テキスト行からエントリIDを作成する。
     *
     * @param lines 看板テキスト行の配列
     * @return パースされたエントリID
     */
    public static @NotNull EntryId fromSignLines(final @Nullable String[] lines) {
        if (lines == null) {
            return blank;
        }
        StringBuilder combined = new StringBuilder();
        for (String line : lines) {
            if (line != null) {
                combined.append(line);
            }
        }
        return from(combined.toString().trim());
    }

    /**
     * 看板テキストからエントリIDを作成する。
     * 有効なSignPicture URLでない場合はnullを返す。
     *
     * @param text 看板テキスト
     * @return パースされたエントリID、無効な場合はnull
     */
    @Nullable
    public static EntryId fromSignText(final @Nullable String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        // 改行を除去して結合
        String combined = text.replace("\n", "").replace("\r", "").trim();
        EntryId id = from(combined);
        return id.isValid() ? id : null;
    }

    /**
     * コンテンツIDを持っているかどうかを判定する。
     *
     * @return URLを含む場合true
     */
    private boolean hasContentId() {
        return !(StringUtils.isEmpty(id())
                || StringUtils.containsOnly(id(), "!")
                || StringUtils.containsOnly(id(), "$"));
    }

    /**
     * メタデータを持っているかどうかを判定する。
     *
     * @return メタデータを含む場合true
     */
    private boolean hasMeta() {
        // 新形式: URL{meta}
        if (hasPrefix() && StringUtils.endsWith(id(), "}") && StringUtils.contains(id(), "{")) {
            return true;
        }
        // 旧形式: URL[meta]
        if (StringUtils.endsWith(id(), "]") && StringUtils.contains(id(), "[")) {
            final String idstr = StringUtils.substring(id(), 0, StringUtils.lastIndexOf(id(), "["));
            return StringUtils.contains(idstr, ".") && StringUtils.contains(idstr, "/");
        }
        // #URL#meta 形式
        if (StringUtils.contains(id(), "#")) {
            return true;
        }
        return false;
    }

    /**
     * 旧形式かどうかを判定する。
     *
     * @return 旧形式の場合true
     */
    public boolean isOutdated() {
        return StringUtils.endsWith(id(), "]") && StringUtils.contains(id(), "[");
    }

    /**
     * プレフィックスを持っているかどうかを判定する。
     *
     * @return プレフィックスがある場合true
     */
    private boolean hasPrefix() {
        final int i = StringUtils.indexOf(id(), "#");
        return 0 <= i && i < 2;
    }

    /**
     * 有効なエントリかどうかを判定する。
     *
     * @return 有効なURLとメタデータを持つ場合true
     */
    public boolean isValid() {
        return hasContentId() && hasMeta();
    }

    /**
     * ContentIdを取得する。
     *
     * @return ContentId、無効な場合はnull
     */
    @Nullable
    public ContentId getContentId() {
        if (hasContentId()) {
            String urlPart;
            if (StringUtils.contains(id(), "[")) {
                // 旧形式
                urlPart = StringUtils.substring(id(), 0, StringUtils.lastIndexOf(id(), "["));
            } else if (hasPrefix() && StringUtils.contains(id(), "{")) {
                // 新形式: #URL{meta}
                urlPart = StringUtils.substring(id(), StringUtils.indexOf(id(), "#") + 1,
                        StringUtils.lastIndexOf(id(), "{"));
            } else if (StringUtils.contains(id(), "#")) {
                // #URL#meta 形式
                int firstHash = StringUtils.indexOf(id(), "#");
                int secondHash = StringUtils.indexOf(id(), "#", firstHash + 1);
                if (secondHash > firstHash) {
                    urlPart = StringUtils.substring(id(), firstHash + 1, secondHash);
                } else {
                    urlPart = id();
                }
            } else {
                urlPart = id();
            }
            return ContentId.from(urlPart);
        }
        return null;
    }

    /**
     * URLを取得する。
     *
     * @return URL文字列、無効な場合はnull
     */
    @Nullable
    public String getUrl() {
        ContentId contentId = getContentId();
        return contentId != null ? contentId.getURI() : null;
    }

    /**
     * メタデータソース文字列を取得する。
     *
     * @return メタデータ文字列、存在しない場合はnull
     */
    @Nullable
    public String getMetaSource() {
        if (hasMeta()) {
            if (StringUtils.endsWith(id(), "}")) {
                // 新形式: {meta}
                return StringUtils.substring(id(),
                        StringUtils.lastIndexOf(id(), "{") + 1,
                        StringUtils.length(id()) - 1);
            } else if (StringUtils.endsWith(id(), "]")) {
                // 旧形式: [meta]
                return StringUtils.substring(id(),
                        StringUtils.lastIndexOf(id(), "[") + 1,
                        StringUtils.length(id()) - 1);
            } else if (StringUtils.contains(id(), "#")) {
                // #URL#meta 形式
                int firstHash = StringUtils.indexOf(id(), "#");
                int secondHash = StringUtils.indexOf(id(), "#", firstHash + 1);
                if (secondHash > firstHash) {
                    return StringUtils.substring(id(), secondHash + 1);
                }
            }
        }
        return null;
    }

    /**
     * プロパティ文字列を取得する (getMetaSourceのエイリアス)。
     *
     * @return プロパティ文字列、存在しない場合はnull
     */
    @Nullable
    public String getProperties() {
        return getMetaSource();
    }

    /**
     * 看板に配置可能かどうかを判定する。
     *
     * @return 配置可能な場合true
     */
    public boolean isPlaceable() {
        return StringUtils.length(id()) <= 15 * 4;
    }

    /**
     * アイテム名として使用可能かどうかを判定する。
     *
     * @return 使用可能な場合true
     */
    public boolean isNameable() {
        return StringUtils.length(id()) <= 40;
    }

    /**
     * 最後の行番号を取得する。
     *
     * @return 行番号 (0-based)
     */
    public int getLastLine() {
        return StringUtils.length(id()) / 15;
    }

    /**
     * このEntryIdに対応するEntryを取得する。
     *
     * @return Entry
     */
    public @NotNull Entry entry() {
        return EntryManager.getInstance().get(this);
    }

    /**
     * プレビュー用のエントリID。
     */
    public static class PreviewEntryId extends EntryId {
        /** プレビューID文字列 */
        public static final @NotNull String PREVIEW_ID = "{#}";

        /** シングルトンインスタンス */
        public static final @NotNull PreviewEntryId instance = new PreviewEntryId();

        private PreviewEntryId() {
            super(PREVIEW_ID);
        }

        @Override
        public @NotNull String id() {
            // TODO: CurrentModeの実装後、現在のモードのEntryIdを返す
            return "";
        }

        @Override
        public String toString() {
            return "PreviewEntryId";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + PREVIEW_ID.hashCode();
            return result;
        }

        @Override
        public boolean equals(@Nullable final Object obj) {
            return this == obj;
        }
    }
}
