package net.teamfruit.signpic.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.teamfruit.signpic.LoadCanceledException;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.Content;
import net.teamfruit.signpic.content.ContentTexture;
import net.teamfruit.signpic.render.VersionCompat;
import net.teamfruit.signpic.state.State;
import net.teamfruit.signpic.state.StateType;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 画像をロードしMinecraftのテクスチャシステムに登録するクラス。
 * レガシー実装のImageIOLoaderに基づき、ImageIO.getImageReaders()で
 * フォーマットを検出する方式を採用。
 */
public class ImageLoader {
    /** テクスチャID生成用カウンター */
    private static final AtomicInteger TEXTURE_COUNTER = new AtomicInteger(0);

    /** 登録済みテクスチャのマップ */
    private static final ConcurrentHashMap<String, ResourceLocation> registeredTextures = new ConcurrentHashMap<>();

    /** GIFアニメーションのデフォルト遅延時間 (秒) */
    public static final float DEFAULT_GIF_DELAY = 0.05f;

    /** キャンセルフラグ */
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    /**
     * 画像読み込みをキャンセルする。
     */
    public void cancel() {
        canceled.set(true);
    }

    /**
     * キャンセル状態をチェックし、キャンセルされていれば例外をスローする。
     */
    private void checkCanceled() throws LoadCanceledException {
        if (canceled.get()) {
            throw new LoadCanceledException();
        }
    }

    /**
     * 指定パスから画像をロードしContentTextureを生成する。
     * レンダースレッドから呼び出す必要がある。
     *
     * @param imagePath 画像ファイルのパス
     * @param state 状態管理オブジェクト
     * @return ロードされたテクスチャ、失敗時はnull
     */
    @Nullable
    public ContentTexture loadImage(Path imagePath, State state) {
        InputStream stream = null;
        ImageInputStream imageStream = null;
        try {
            state.setType(StateType.LOADING);
            state.setMessage("Loading image...");

            // ImageInputStreamを作成
            stream = new FileInputStream(imagePath.toFile());
            imageStream = ImageIO.createImageInputStream(stream);

            // ImageReaderを取得 (フォーマット自動検出)
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
            if (!readers.hasNext()) {
                throw new InvaildImageException();
            }

            ImageReader reader = readers.next();
            String formatName = reader.getFormatName().toLowerCase();

            SignPicture.LOGGER.debug("画像フォーマット検出: {}", formatName);

            // GIF判定とロード処理の分岐
            ContentTexture texture;
            if ("gif".equals(formatName)) {
                texture = loadGif(reader, imageStream, state);
            } else {
                texture = loadSingleImage(reader, imageStream, state);
            }

            state.setType(StateType.LOADED);
            state.setMessage(null);

            SignPicture.LOGGER.info("画像ロード完了: {} ({})", imagePath, formatName);

            return texture;
        } catch (InvaildImageException e) {
            SignPicture.LOGGER.error("画像形式が無効: {}", imagePath, e);
            state.setError(e);
            return null;
        } catch (LoadCanceledException e) {
            SignPicture.LOGGER.info("画像ロードがキャンセルされました: {}", imagePath);
            state.setError(e);
            return null;
        } catch (Exception e) {
            SignPicture.LOGGER.error("画像ロード失敗: {}", imagePath, e);
            state.setError(e);
            return null;
        } finally {
            closeQuietly(imageStream);
            closeQuietly(stream);
        }
    }

    /**
     * 単一画像をロードする。
     *
     * @param reader ImageReader
     * @param imageStream 画像入力ストリーム
     * @param state 状態管理オブジェクト
     * @return ロードされたテクスチャ
     */
    private ContentTexture loadSingleImage(ImageReader reader, ImageInputStream imageStream, State state) throws Exception {
        try {
            ImageReadParam param = reader.getDefaultReadParam();
            reader.setInput(imageStream, true, true);

            BufferedImage bufferedImage = reader.read(0, param);
            if (bufferedImage == null) {
                throw new InvaildImageException("ImageReader returned null");
            }

            // BufferedImage → NativeImage変換
            NativeImage nativeImage = convertToNativeImage(bufferedImage);
            if (nativeImage == null) {
                throw new InvaildImageException("Failed to convert BufferedImage to NativeImage");
            }

            int width = nativeImage.getWidth();
            int height = nativeImage.getHeight();

            // テクスチャ登録
            String textureKey = generateTextureKey(String.valueOf(System.nanoTime()));
            ResourceLocation textureLocation = getOrCreateTextureLocation(textureKey);

            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);
            registeredTextures.put(textureKey, textureLocation);

            // 単一フレームのテクスチャ作成
            List<ContentTexture.Frame> frames = new ArrayList<>();
            frames.add(new ContentTexture.Frame(textureLocation, 0));

            return new ContentTexture(frames, width, height);
        } finally {
            reader.dispose();
        }
    }

    /**
     * GIFアニメーションをロードする。
     * 複数フレームを持つGIF画像を処理し、各フレームをテクスチャとして登録する。
     *
     * @param reader ImageReader
     * @param imageStream 画像入力ストリーム
     * @param state 状態管理オブジェクト
     * @return ロードされたアニメーションテクスチャ
     */
    private ContentTexture loadGif(ImageReader reader, ImageInputStream imageStream, State state) throws Exception {
        try {
            reader.setInput(imageStream, false, false);

            int numFrames = reader.getNumImages(true);
            if (numFrames == 0) {
                throw new InvaildImageException("GIF has no frames");
            }

            SignPicture.LOGGER.debug("GIFフレーム数: {}", numFrames);

            List<ContentTexture.Frame> frames = new ArrayList<>();
            int width = 0;
            int height = 0;

            for (int i = 0; i < numFrames; i++) {
                checkCanceled();

                BufferedImage frameImage = reader.read(i);
                if (frameImage == null) {
                    continue;
                }

                if (i == 0) {
                    width = frameImage.getWidth();
                    height = frameImage.getHeight();
                }

                // フレーム遅延時間の取得 (ミリ秒)
                int delayMs = getGifFrameDelay(reader, i);

                // BufferedImage → NativeImage変換
                NativeImage nativeImage = convertToNativeImage(frameImage);
                if (nativeImage == null) {
                    continue;
                }

                // テクスチャ登録
                String textureKey = generateTextureKey(System.nanoTime() + "_frame_" + i);
                ResourceLocation textureLocation = getOrCreateTextureLocation(textureKey);

                DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
                Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);
                registeredTextures.put(textureKey, textureLocation);

                frames.add(new ContentTexture.Frame(textureLocation, delayMs));
            }

            if (frames.isEmpty()) {
                throw new InvaildImageException("Failed to load any GIF frames");
            }

            return new ContentTexture(frames, width, height);
        } finally {
            reader.dispose();
        }
    }

    /**
     * GIFフレームの遅延時間を取得する (ミリ秒)。
     *
     * @param reader ImageReader
     * @param frameIndex フレームインデックス
     * @return 遅延時間 (ミリ秒)
     */
    private int getGifFrameDelay(ImageReader reader, int frameIndex) {
        try {
            // GIFメタデータから遅延時間を取得
            javax.imageio.metadata.IIOMetadata metadata = reader.getImageMetadata(frameIndex);
            if (metadata != null) {
                String[] names = metadata.getMetadataFormatNames();
                for (String name : names) {
                    org.w3c.dom.Node root = metadata.getAsTree(name);
                    int delay = findGifDelay(root);
                    if (delay > 0) {
                        return delay * 10; // GIFの遅延は1/100秒単位
                    }
                }
            }
        } catch (Exception e) {
            SignPicture.LOGGER.debug("GIFフレーム遅延取得失敗: {}", e.getMessage());
        }
        // デフォルト遅延 (50ms)
        return (int) (DEFAULT_GIF_DELAY * 1000);
    }

    /**
     * GIFメタデータノードから遅延時間を再帰的に検索する。
     */
    private int findGifDelay(org.w3c.dom.Node node) {
        if ("GraphicControlExtension".equals(node.getNodeName())) {
            org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
            if (attrs != null) {
                org.w3c.dom.Node delayNode = attrs.getNamedItem("delayTime");
                if (delayNode != null) {
                    try {
                        return Integer.parseInt(delayNode.getNodeValue());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // 子ノードを再帰的に検索
        org.w3c.dom.NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            int delay = findGifDelay(children.item(i));
            if (delay > 0) {
                return delay;
            }
        }
        return 0;
    }

    /**
     * BufferedImageをNativeImageに変換する。
     * PNG形式を経由して変換を行う。
     *
     * @param bufferedImage 変換元のBufferedImage
     * @return 変換されたNativeImage、失敗時はnull
     */
    @Nullable
    private NativeImage convertToNativeImage(BufferedImage bufferedImage) {
        try {
            // ARGBフォーマットに変換 (アルファチャンネル確保)
            BufferedImage argbImage = new BufferedImage(
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );
            argbImage.getGraphics().drawImage(bufferedImage, 0, 0, null);

            // PNG形式でバイト配列に書き出し
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(argbImage, "PNG", baos);
            byte[] pngBytes = baos.toByteArray();

            // NativeImageとして読み込み
            try (ByteArrayInputStream bais = new ByteArrayInputStream(pngBytes)) {
                return NativeImage.read(bais);
            }
        } catch (Exception e) {
            SignPicture.LOGGER.error("BufferedImage→NativeImage変換失敗", e);
            return null;
        }
    }

    /**
     * 画像のスケジュールロードを行う。
     * メインスレッドで画像をロードし、Contentにテクスチャを設定する。
     *
     * @param content ロード対象のContent
     */
    public static void scheduleLoad(Content content) {
        Path cachedFile = content.getCachedFile();
        if (cachedFile == null || !cachedFile.toFile().exists()) {
            return;
        }

        // レンダースレッドでロードをスケジュール
        Minecraft.getInstance().execute(() -> {
            ImageLoader loader = new ImageLoader();
            ContentTexture texture = loader.loadImage(cachedFile, content.getState());
            if (texture != null) {
                content.setTexture(texture);
            }
        });
    }

    /**
     * テクスチャキーを生成する (SHA-256ハッシュベース)。
     */
    private static String generateTextureKey(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "img_" + TEXTURE_COUNTER.incrementAndGet();
        }
    }

    /**
     * テクスチャのResourceLocationを取得または作成する。
     */
    private static ResourceLocation getOrCreateTextureLocation(String key) {
        ResourceLocation existing = registeredTextures.get(key);
        if (existing != null) {
            return existing;
        }
        return VersionCompat.createModResourceLocation("dynamic/" + key);
    }

    /**
     * テクスチャをMinecraftのテクスチャマネージャから解除する。
     *
     * @param location 解除するテクスチャのResourceLocation
     */
    public static void unregisterTexture(ResourceLocation location) {
        if (location != null) {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().getTextureManager().release(location);
            });
        }
    }

    /**
     * 登録済みの動的テクスチャをすべてクリアする。
     */
    public static void clearAll() {
        for (ResourceLocation location : registeredTextures.values()) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
        registeredTextures.clear();
    }

    /**
     * AutoCloseableを静かにクローズする。
     */
    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
