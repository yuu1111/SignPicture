package net.teamfruit.signpic.content;

import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.config.SignPicConfig;
import net.teamfruit.signpic.entry.EntrySlot;
import net.teamfruit.signpic.entry.ICollectable;
import net.teamfruit.signpic.http.Communicator;
import net.teamfruit.signpic.http.ContentDownloader;
import net.teamfruit.signpic.image.ImageLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * コンテンツの読み込み、キャッシュ、ガベージコレクションを管理するクラス。
 */
public class ContentManager {
    /** シングルトンインスタンス */
    public static @NotNull ContentManager instance = new ContentManager();

    /** キャッシュディレクトリ */
    private static Path cacheDirectory;

    /**
     * シングルトンインスタンスを取得する。
     *
     * @return ContentManagerインスタンス
     */
    public static ContentManager getInstance() {
        return instance;
    }

    /**
     * キャッシュディレクトリを設定する。
     *
     * @param directory キャッシュディレクトリ
     */
    public static void setCacheDirectory(Path directory) {
        cacheDirectory = directory;
    }

    /**
     * キャッシュディレクトリを取得する。
     *
     * @return キャッシュディレクトリ
     */
    public static Path getCacheDirectory() {
        return cacheDirectory;
    }

    /** コンテンツレジストリ */
    private final @NotNull Map<ContentId, ContentSlot> registry = new ConcurrentHashMap<>();

    /** 初期化待ちキュー */
    private final @NotNull Queue<ContentSlot> loadqueue = new ConcurrentLinkedQueue<>();

    /** ロードティックカウンター */
    private int loadtick = 0;

    private ContentManager() {}

    /**
     * ContentIdに対応するContentを取得する。
     * 存在しない場合は作成する。
     *
     * @param id ContentId
     * @return Content
     */
    public @NotNull Content get(final @NotNull ContentId id) {
        final ContentSlot entries = this.registry.get(id);
        if (entries != null) {
            return entries.get();
        } else {
            final Content entry = new Content(id);
            final ContentSlot slot = new ContentSlot(entry);
            this.registry.put(id, slot);
            this.loadqueue.offer(slot);
            return entry;
        }
    }

    /**
     * URL文字列からContentを取得または作成する。
     *
     * @param url URL文字列
     * @return Content
     */
    public @NotNull Content getOrCreate(String url) {
        return get(ContentId.from(url));
    }

    /**
     * URL文字列から既存のContentを取得する。
     *
     * @param url URL文字列
     * @return Content、存在しない場合はnull
     */
    @Nullable
    public Content getByUrl(String url) {
        ContentId id = ContentId.from(url);
        ContentSlot slot = registry.get(id);
        return slot != null ? slot.get() : null;
    }

    /**
     * ティック処理。
     * ロードキューの処理とGCを行う。
     */
    public void onTick() {
        // ロードキュー処理
        this.loadtick++;
        if (this.loadtick > SignPicConfig.get().contentLoadTick) {
            this.loadtick = 0;
            final ContentSlot loadprogress = this.loadqueue.poll();
            if (loadprogress != null) {
                scheduleLoad(loadprogress.get());
            }
        }

        // GC処理
        for (final Iterator<Map.Entry<ContentId, ContentSlot>> itr = this.registry.entrySet().iterator(); itr.hasNext();) {
            final Map.Entry<ContentId, ContentSlot> entry = itr.next();
            final ContentSlot collectableSlot = entry.getValue();

            if (collectableSlot.shouldCollect()) {
                this.loadqueue.remove(collectableSlot);
                collectableSlot.get().onCollect();
                itr.remove();
            }
        }
    }

    /**
     * コンテンツのロードをスケジュールする。
     *
     * @param content コンテンツ
     */
    private void scheduleLoad(Content content) {
        content.onInit();

        Path cacheFile = getCacheFile(content.getUrl());
        content.setCachedFile(cacheFile);

        // キャッシュ確認
        if (cacheFile != null && cacheFile.toFile().exists()) {
            SignPicture.LOGGER.debug("キャッシュから読み込み: {}", cacheFile);
            ImageLoader.scheduleLoad(content);
        } else if (cacheFile != null) {
            // ダウンロード後にロード
            ContentDownloader downloader = new ContentDownloader(
                    content.getUrl(),
                    cacheFile,
                    content.getState(),
                    () -> ImageLoader.scheduleLoad(content)
            );
            Communicator.getInstance().submit(downloader);
        }
    }

    /**
     * URLからキャッシュファイルパスを生成する。
     *
     * @param url URL文字列
     * @return キャッシュファイルパス
     */
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

            // URLから拡張子を取得
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
            SignPicture.LOGGER.error("キャッシュパス生成に失敗", e);
            return null;
        }
    }

    /**
     * 全コンテンツを再読み込みする。
     */
    public void reloadAll() {
        for (Map.Entry<ContentId, ContentSlot> entry : registry.entrySet()) {
            entry.getValue().get().markDirty();
        }
    }

    /**
     * 全コンテンツを再ダウンロードする。
     */
    public void redownloadAll() {
        for (Map.Entry<ContentId, ContentSlot> entry : registry.entrySet()) {
            entry.getValue().get().markDirtyWithCache();
        }
    }

    /**
     * すべてのコンテンツを削除し、リソースを解放する。
     */
    public void clear() {
        for (ContentSlot slot : registry.values()) {
            slot.get().dispose();
        }
        registry.clear();
    }

    /**
     * テクスチャをクリアし、キャッシュから再読み込みをスケジュールする。
     */
    public void clearTextures() {
        for (ContentSlot slot : registry.values()) {
            Content content = slot.get();
            ContentTexture texture = content.getTexture();
            if (texture != null) {
                content.setTexture(null);
                if (content.getCachedFile() != null && content.getCachedFile().toFile().exists()) {
                    ImageLoader.scheduleLoad(content);
                }
            }
        }
    }

    /**
     * ガベージコレクションを実行する (onTick()へのエイリアス)。
     */
    public void gc() {
        onTick();
    }

    /**
     * コンテンツ数を取得する。
     *
     * @return コンテンツ数
     */
    public int getContentCount() {
        return registry.size();
    }

    /**
     * コンテンツ用のスロット。
     */
    public static class ContentSlot extends EntrySlot<Content> implements ICollectable {
        public ContentSlot(final @NotNull Content entry) {
            super(entry);
        }

        @Override
        public void onCollect() {
            this.entry.onCollect();
        }

        @Override
        public boolean shouldCollect() {
            return this.entry.shouldCollect() || super.shouldCollect();
        }

        @Override
        protected int getCollectTimes() {
            return SignPicConfig.get().contentGcDelayTicks;
        }
    }
}
