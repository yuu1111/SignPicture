package net.teamfruit.signpic.http;

import net.teamfruit.signpic.LoadCanceledException;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.config.SignPicConfig;
import net.teamfruit.signpic.content.ContentCapacityOverException;
import net.teamfruit.signpic.state.Progress;
import net.teamfruit.signpic.state.State;
import net.teamfruit.signpic.state.StateType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * URLからローカルファイルへコンテンツをダウンロードするクラス。
 * レガシーのContentDownload.javaに基づく実装。
 */
public class ContentDownloader implements Communicator.CommunicateTask {
    /** バッファサイズ (8KB) */
    private static final int BUFFER_SIZE = 8192;

    /** ダウンロード元URL */
    private final String url;

    /** 保存先パス */
    private final Path destination;

    /** 状態管理オブジェクト */
    private final State state;

    /** 完了時コールバック */
    private final Runnable onComplete;

    /** キャンセルフラグ */
    private volatile boolean cancelled = false;

    /**
     * ContentDownloaderを構築する。
     *
     * @param url ダウンロード元URL
     * @param destination 保存先パス
     * @param state 状態管理オブジェクト
     */
    public ContentDownloader(String url, Path destination, State state) {
        this(url, destination, state, null);
    }

    /**
     * ContentDownloaderを構築する。
     *
     * @param url ダウンロード元URL
     * @param destination 保存先パス
     * @param state 状態管理オブジェクト
     * @param onComplete 完了時コールバック
     */
    public ContentDownloader(String url, Path destination, State state, Runnable onComplete) {
        this.url = url;
        this.destination = destination;
        this.state = state;
        this.onComplete = onComplete;
    }

    /**
     * ダウンロードを実行する。
     * HTTP接続を確立し、コンテンツをファイルに保存する。
     *
     * @throws Exception ダウンロード中にエラーが発生した場合
     */
    @Override
    public void execute() throws Exception {
        SignPicture.LOGGER.debug("ダウンロード開始: {}", url);
        state.setType(StateType.DOWNLOADING);
        state.setProgress(new Progress());

        HttpURLConnection connection = null;
        try {
            URL urlObj = new URL(url);
            connection = (HttpURLConnection) urlObj.openConnection();
            connection.setConnectTimeout(SignPicConfig.get().httpTimeout);
            connection.setReadTimeout(SignPicConfig.get().httpTimeout);
            connection.setRequestProperty("User-Agent", "SignPicture/3.0");
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTPエラー: " + responseCode);
            }

            // Content-Typeが画像であることを検証
            String contentType = connection.getContentType();
            if (contentType != null && !isImageContentType(contentType)) {
                throw new InvalidContentTypeException(contentType);
            }

            // コンテンツサイズチェック
            long contentLength = connection.getContentLengthLong();
            int maxBytes = SignPicConfig.get().contentMaxBytes;
            if (maxBytes > 0 && contentLength > maxBytes) {
                throw new ContentCapacityOverException(contentLength, maxBytes);
            }

            state.getProgress().overall = contentLength;

            // 親ディレクトリを作成
            Files.createDirectories(destination.getParent());

            // 一時ファイルにダウンロード
            Path tempFile = destination.resolveSibling(destination.getFileName() + ".tmp");

            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(tempFile))) {

                byte[] buffer = new byte[BUFFER_SIZE];
                long totalRead = 0;
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    // キャンセルチェック
                    if (cancelled) {
                        throw new LoadCanceledException();
                    }

                    out.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                    state.getProgress().done = totalRead;

                    // ダウンロード中のサイズチェック
                    if (maxBytes > 0 && totalRead > maxBytes) {
                        throw new ContentCapacityOverException(totalRead, maxBytes);
                    }
                }
            }

            // 一時ファイルを最終ファイルに移動
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
            SignPicture.LOGGER.debug("ダウンロード完了: {}", destination);

            // 完了コールバックを呼び出し
            if (onComplete != null) {
                onComplete.run();
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * エラー発生時のハンドラ。
     *
     * @param e 発生した例外
     */
    @Override
    public void onError(Exception e) {
        state.setError(e);
        SignPicture.LOGGER.error("ダウンロード失敗: {}", url, e);
    }

    /**
     * ダウンロードをキャンセルする。
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Content-Typeが画像かどうかを判定する。
     *
     * @param contentType Content-Type文字列
     * @return 画像の場合true
     */
    private static boolean isImageContentType(String contentType) {
        if (contentType == null) {
            return true; // 指定なしの場合は許可
        }
        String type = contentType.toLowerCase();
        return type.startsWith("image/") ||
               type.contains("octet-stream"); // バイナリストリームも許可
    }

    /**
     * 無効なContent-Typeの場合にスローされる例外。
     */
    public static class InvalidContentTypeException extends IOException {
        public InvalidContentTypeException(String contentType) {
            super("無効なContent-Type: " + contentType + " (画像が期待されます)");
        }
    }
}
