package net.teamfruit.signpic.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.Content;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.content.ContentTexture;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.image.ImageLoader;
import net.teamfruit.signpic.state.StateType;
import org.jetbrains.annotations.Nullable;

/**
 * SignPictureのメインGUI画面。
 * 画像のプレビュー表示と看板への適用機能を提供する。
 */
public class SignPicScreen extends Screen {
    /** URL入力フィールド */
    private EditBox urlField;

    /** ステータステキスト */
    private String statusText = "";

    /** ステータステキストの色 */
    private int statusColor = 0xFFFF00;

    /** 現在ロード中/表示中のコンテンツ */
    @Nullable
    private Content currentContent;

    /** アニメーション時間 (GIF再生用) */
    private float animationTime = 0;

    /** プレビューサイズ (ピクセル) */
    private static final int PREVIEW_SIZE = 128;

    /** プレビュー表示位置 */
    private int previewX, previewY;

    /**
     * SignPicScreenを構築する。
     */
    public SignPicScreen() {
        super(Component.literal("SignPicture"));
    }

    /**
     * 画面の初期化処理。
     * ウィジェット (入力フィールド、ボタン) を配置する。
     */
    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = 40;

        previewX = centerX - 180;
        previewY = startY + 30;

        // URL入力フィールド
        this.urlField = new EditBox(this.font, centerX - 150, startY, 300, 20, Component.literal("URL"));
        this.urlField.setMaxLength(2048);
        this.urlField.setHint(Component.literal("画像URLを入力..."));
        this.addWidget(this.urlField);

        int buttonY = startY + 25;
        int buttonWidth = 72;
        int buttonSpacing = 2;
        int buttonsStartX = centerX - 150;

        // 行1: 読込、URL複製、看板に適用、URLを開く
        this.addRenderableWidget(Button.builder(Component.literal("読込"), button -> {
            loadImage();
        }).bounds(buttonsStartX, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("URL複製"), button -> {
            copyToClipboard();
        }).bounds(buttonsStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("看板に適用"), button -> {
            applyToSign();
        }).bounds(buttonsStartX + (buttonWidth + buttonSpacing) * 2, buttonY, buttonWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("URLを開く"), button -> {
            openUrl();
        }).bounds(buttonsStartX + (buttonWidth + buttonSpacing) * 3, buttonY, buttonWidth, 20).build());

        // 行2: キャッシュクリア、再読込、閉じる
        buttonY += 25;
        this.addRenderableWidget(Button.builder(Component.literal("キャッシュクリア"), button -> {
            clearCache();
        }).bounds(buttonsStartX, buttonY, 98, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("再読込"), button -> {
            reloadTextures();
        }).bounds(buttonsStartX + 100, buttonY, 98, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("閉じる"), button -> {
            this.onClose();
        }).bounds(buttonsStartX + 200, buttonY, 98, 20).build());

        updateStatus();
    }

    /**
     * 画像をロードする。
     * URL入力フィールドの内容からコンテンツを取得/作成する。
     */
    private void loadImage() {
        String url = urlField.getValue().trim();
        if (!url.isEmpty()) {
            currentContent = ContentManager.getInstance().getOrCreate(url);
            setStatus("読込中...", 0xFFFF00);
            SignPicture.LOGGER.info("GUIから画像読込: {}", url);
        } else {
            setStatus("URLを入力してください", 0xFF5555);
        }
    }

    /**
     * URLをクリップボードにコピーする。
     */
    private void copyToClipboard() {
        String url = urlField.getValue().trim();
        if (!url.isEmpty()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(url);
            setStatus("クリップボードにコピーしました!", 0x55FF55);
        } else {
            setStatus("コピーするURLがありません", 0xFF5555);
        }
    }

    /**
     * URLを看板に適用する。
     * プレイヤーが看板を見ている場合、URLをクリップボードにコピーして
     * 看板編集画面で貼り付けるよう案内する。
     */
    private void applyToSign() {
        String url = urlField.getValue().trim();
        if (url.isEmpty()) {
            setStatus("先にURLを入力してください", 0xFF5555);
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            setStatus("ワールドに入っていません", 0xFF5555);
            return;
        }

        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            setStatus("看板を見てください", 0xFF5555);
            return;
        }

        BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
        BlockEntity blockEntity = mc.level.getBlockEntity(blockPos);

        if (!(blockEntity instanceof SignBlockEntity)) {
            setStatus("看板を見てください", 0xFF5555);
            return;
        }

        Minecraft.getInstance().keyboardHandler.setClipboard(url);
        setStatus("URLをコピーしました! 看板を右クリックして貼り付け", 0x55FF55);
        SignPicture.LOGGER.info("看板用にURLをコピー: {}", blockPos);
    }

    /**
     * URLをブラウザで開く。
     */
    private void openUrl() {
        String url = urlField.getValue().trim();
        if (!url.isEmpty()) {
            try {
                net.minecraft.Util.getPlatform().openUri(url);
                setStatus("ブラウザで開いています...", 0x55FF55);
            } catch (Exception e) {
                setStatus("URLを開けませんでした", 0xFF5555);
            }
        } else {
            setStatus("開くURLがありません", 0xFF5555);
        }
    }

    /**
     * キャッシュをクリアする。
     * すべての画像テクスチャ、コンテンツ、エントリをクリアする。
     */
    private void clearCache() {
        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        ImageLoader.clearAll();
        ContentManager.getInstance().clear();
        EntryManager.getInstance().clear();
        currentContent = null;

        setStatus("画像" + contentCount + "件、エントリ" + entryCount + "件をクリア", 0x55FF55);
        SignPicture.LOGGER.info("GUIからキャッシュクリア");
    }

    /**
     * テクスチャを再読込する。
     */
    private void reloadTextures() {
        ImageLoader.clearAll();
        ContentManager.getInstance().clearTextures();
        setStatus("テクスチャ再読込中...", 0xFFFF00);
        SignPicture.LOGGER.info("GUIからテクスチャ再読込");
    }

    /**
     * ステータステキストを設定する。
     *
     * @param text 表示テキスト
     * @param color 色 (0xRRGGBB形式)
     */
    private void setStatus(String text, int color) {
        this.statusText = text;
        this.statusColor = color;
    }

    /**
     * 現在のコンテンツ状態に基づいてステータスを更新する。
     */
    private void updateStatus() {
        if (currentContent != null) {
            StateType stateType = currentContent.getState().getType();
            switch (stateType) {
                case WAITING:
                case DOWNLOADING:
                    setStatus("ダウンロード中...", 0xFFFF00);
                    break;
                case LOADING:
                    setStatus("テクスチャ読込中...", 0xFFFF00);
                    break;
                case LOADED:
                    ContentTexture tex = currentContent.getTexture();
                    if (tex != null) {
                        setStatus("読込完了: " + tex.getWidth() + "x" + tex.getHeight(), 0x55FF55);
                    } else {
                        setStatus("読込完了", 0x55FF55);
                    }
                    break;
                case ERROR:
                    setStatus("画像読込エラー", 0xFF5555);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 画面を描画する。
     *
     * @param guiGraphics 描画コンテキスト
     * @param mouseX マウスX座標
     * @param mouseY マウスY座標
     * @param partialTick 部分ティック
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        animationTime += partialTick / 20f;

        // 背景描画
        guiGraphics.fill(0, 0, this.width, this.height, 0xC0101010);

        // タイトル描画
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        // バージョン表示
        String version = "v" + SignPicture.MOD_VERSION;
        guiGraphics.drawString(this.font, version, this.width - this.font.width(version) - 5, 5, 0x808080);

        // プレビュー描画
        renderPreview(guiGraphics);

        // 情報表示
        int infoX = this.width / 2 + 20;
        int infoY = previewY;

        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        guiGraphics.drawString(this.font, "キャッシュ画像: " + contentCount, infoX, infoY, 0xAAAAAA);
        guiGraphics.drawString(this.font, "アクティブエントリ: " + entryCount, infoX, infoY + 12, 0xAAAAAA);

        // 視線先の情報表示
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
            if (mc.level != null && mc.level.getBlockEntity(pos) instanceof SignBlockEntity) {
                guiGraphics.drawString(this.font, "視線先: 看板", infoX, infoY + 30, 0x55FF55);
            } else {
                guiGraphics.drawString(this.font, "視線先: 看板以外", infoX, infoY + 30, 0x888888);
            }
        } else {
            guiGraphics.drawString(this.font, "視線先: なし", infoX, infoY + 30, 0x888888);
        }

        // ステータステキスト描画
        if (!statusText.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, statusText, this.width / 2, this.height - 30, statusColor);
        }

        // URL入力フィールド描画
        this.urlField.render(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        updateStatus();
    }

    /**
     * プレビュー領域を描画する。
     * 現在のコンテンツの画像またはステータスを表示する。
     *
     * @param guiGraphics 描画コンテキスト
     */
    private void renderPreview(GuiGraphics guiGraphics) {
        int x = previewX;
        int y = previewY;
        int size = PREVIEW_SIZE;

        // プレビュー枠描画
        guiGraphics.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0xFF444444);
        guiGraphics.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF222222);

        if (currentContent != null) {
            ContentTexture texture = currentContent.getTexture();
            if (texture != null) {
                ContentTexture.Frame frame = texture.getFrameForTime(animationTime);
                if (frame != null) {
                    // アスペクト比を維持して描画
                    float aspectRatio = (float) texture.getWidth() / texture.getHeight();
                    int drawWidth, drawHeight;
                    int drawX, drawY;

                    if (aspectRatio > 1) {
                        drawWidth = size;
                        drawHeight = (int) (size / aspectRatio);
                        drawX = x;
                        drawY = y + (size - drawHeight) / 2;
                    } else {
                        drawHeight = size;
                        drawWidth = (int) (size * aspectRatio);
                        drawX = x + (size - drawWidth) / 2;
                        drawY = y;
                    }

                    RenderSystem.setShaderTexture(0, frame.getTextureLocation());
                    guiGraphics.blit(frame.getTextureLocation(), drawX, drawY, 0, 0, drawWidth, drawHeight, drawWidth, drawHeight);

                    // サイズ情報表示
                    String dims = texture.getWidth() + "x" + texture.getHeight();
                    if (texture.isAnimated()) {
                        dims += " (アニメーション)";
                    }
                    guiGraphics.drawCenteredString(this.font, dims, x + size / 2, y + size + 5, 0x888888);
                    return;
                }
            }

            // テクスチャがない場合は状態を表示
            StateType state = currentContent.getState().getType();
            String stateText = switch (state) {
                case WAITING -> "待機中...";
                case DOWNLOADING -> "ダウンロード中...";
                case LOADING -> "読込中...";
                case ERROR -> "エラー";
                default -> "...";
            };
            guiGraphics.drawCenteredString(this.font, stateText, x + size / 2, y + size / 2, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(this.font, "プレビューなし", x + size / 2, y + size / 2, 0x666666);
        }
    }

    /**
     * この画面がゲームを一時停止するかどうか。
     *
     * @return false (一時停止しない)
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * ティック処理。
     */
    @Override
    public void tick() {
        super.tick();
    }

    /**
     * キー押下イベント処理。
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.urlField.isFocused()) {
            return this.urlField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * 文字入力イベント処理。
     */
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.urlField.isFocused()) {
            return this.urlField.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    /**
     * マウスクリックイベント処理。
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.urlField.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * SignPicScreen画面を開く。
     */
    public static void open() {
        Minecraft.getInstance().setScreen(new SignPicScreen());
    }
}
