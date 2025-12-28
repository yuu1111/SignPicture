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
import net.teamfruit.signpic.attr.SignPicProperties;
import net.teamfruit.signpic.content.Content;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.content.ContentTexture;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.image.ImageLoader;
import net.teamfruit.signpic.state.StateType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * SignPictureのメインGUI画面。
 * 画像のプレビュー表示、プロパティ編集、看板への適用機能を提供する。
 */
public class SignPicScreen extends Screen {
    /** URL入力フィールド */
    private EditBox urlField;

    /** プロパティ入力フィールド */
    private EditBox widthField;
    private EditBox heightField;
    private EditBox offsetXField;
    private EditBox offsetYField;
    private EditBox offsetZField;
    private EditBox rotationField;

    /** 全入力フィールドのリスト (フォーカス管理用) */
    private final List<EditBox> allFields = new ArrayList<>();

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
    private static final int PREVIEW_SIZE = 100;

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
        allFields.clear();

        int centerX = this.width / 2;
        int startY = 30;

        // URL入力フィールド
        this.urlField = new EditBox(this.font, centerX - 150, startY, 300, 18, Component.literal("URL"));
        this.urlField.setMaxLength(2048);
        this.urlField.setHint(Component.literal("画像URLを入力..."));
        this.addWidget(this.urlField);
        allFields.add(this.urlField);

        int buttonY = startY + 22;
        int buttonWidth = 60;
        int buttonSpacing = 2;
        int buttonsStartX = centerX - 150;

        // 行1: 読込、URL複製、看板に適用、URLを開く、閉じる
        this.addRenderableWidget(Button.builder(Component.literal("読込"), button -> {
            loadImage();
        }).bounds(buttonsStartX, buttonY, buttonWidth, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("複製"), button -> {
            copyToClipboard();
        }).bounds(buttonsStartX + buttonWidth + buttonSpacing, buttonY, buttonWidth, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("適用"), button -> {
            applyToSign();
        }).bounds(buttonsStartX + (buttonWidth + buttonSpacing) * 2, buttonY, buttonWidth, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("開く"), button -> {
            openUrl();
        }).bounds(buttonsStartX + (buttonWidth + buttonSpacing) * 3, buttonY, buttonWidth, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("閉じる"), button -> {
            this.onClose();
        }).bounds(buttonsStartX + (buttonWidth + buttonSpacing) * 4, buttonY, buttonWidth, 18).build());

        // プレビュー位置
        previewX = centerX - 145;
        previewY = buttonY + 25;

        // プロパティ編集パネル (プレビューの右側)
        int propX = previewX + PREVIEW_SIZE + 15;
        int propY = previewY;
        int propFieldWidth = 40;
        int propLabelWidth = 20;

        // サイズ: W / H
        guiGraphics_drawString_placeholder = propY;
        this.widthField = createPropertyField(propX + propLabelWidth, propY, propFieldWidth, "");
        this.heightField = createPropertyField(propX + propLabelWidth + propFieldWidth + propLabelWidth + 5, propY, propFieldWidth, "");

        propY += 22;
        // オフセット: X / Y / Z
        this.offsetXField = createPropertyField(propX + propLabelWidth, propY, propFieldWidth - 5, "0");
        this.offsetYField = createPropertyField(propX + propLabelWidth + propFieldWidth + 5, propY, propFieldWidth - 5, "0");
        this.offsetZField = createPropertyField(propX + propLabelWidth + (propFieldWidth + 5) * 2, propY, propFieldWidth - 5, "0");

        propY += 22;
        // 回転: R
        this.rotationField = createPropertyField(propX + propLabelWidth, propY, propFieldWidth, "0");

        propY += 25;
        // 生成ボタン
        this.addRenderableWidget(Button.builder(Component.literal("URL生成"), button -> {
            generateSignText();
        }).bounds(propX, propY, 80, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("クリア"), button -> {
            clearProperties();
        }).bounds(propX + 85, propY, 55, 18).build());

        // 下部ボタン行
        int bottomY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("キャッシュクリア"), button -> {
            clearCache();
        }).bounds(centerX - 150, bottomY, 98, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("再読込"), button -> {
            reloadTextures();
        }).bounds(centerX - 50, bottomY, 98, 20).build());

        updateStatus();
    }

    /** プレースホルダー変数 (描画用) */
    private int guiGraphics_drawString_placeholder;

    /**
     * プロパティ入力フィールドを作成する。
     */
    private EditBox createPropertyField(int x, int y, int width, String defaultValue) {
        EditBox field = new EditBox(this.font, x, y, width, 16, Component.empty());
        field.setMaxLength(10);
        field.setValue(defaultValue);
        this.addWidget(field);
        allFields.add(field);
        return field;
    }

    /**
     * プロパティからURL+パラメータ文字列を生成する。
     */
    private void generateSignText() {
        String url = urlField.getValue().trim();
        if (url.isEmpty()) {
            setStatus("URLを入力してください", 0xFF5555);
            return;
        }

        // URLから既存のプロパティを除去
        int hashIndex = url.indexOf('#');
        if (hashIndex > 0) {
            url = url.substring(0, hashIndex);
        }

        SignPicProperties props = new SignPicProperties();

        // 幅
        float w = parseFloat(widthField.getValue(), -1);
        if (w > 0) props.setWidth(w);

        // 高さ
        float h = parseFloat(heightField.getValue(), -1);
        if (h > 0) props.setHeight(h);

        // オフセット
        props.setOffsetX(parseFloat(offsetXField.getValue(), 0));
        props.setOffsetY(parseFloat(offsetYField.getValue(), 0));
        props.setOffsetZ(parseFloat(offsetZField.getValue(), 0));

        // 回転
        props.setRotationZ(parseFloat(rotationField.getValue(), 0));

        String signText = props.toSignText(url);
        urlField.setValue(signText);
        Minecraft.getInstance().keyboardHandler.setClipboard(signText);
        setStatus("生成してクリップボードにコピー: " + props.toPropertyString(), 0x55FF55);
    }

    /**
     * プロパティフィールドをクリアする。
     */
    private void clearProperties() {
        widthField.setValue("");
        heightField.setValue("");
        offsetXField.setValue("0");
        offsetYField.setValue("0");
        offsetZField.setValue("0");
        rotationField.setValue("0");
        setStatus("プロパティをクリアしました", 0x55FF55);
    }

    /**
     * 文字列を浮動小数点数にパースする。
     */
    private float parseFloat(String value, float defaultValue) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 画像をロードする。
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
            setStatus("クリップボードにコピー!", 0x55FF55);
        } else {
            setStatus("コピーするURLがありません", 0xFF5555);
        }
    }

    /**
     * URLを看板に適用する。
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
        setStatus("コピー完了! 看板を右クリックして貼り付け", 0x55FF55);
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
                case INIT:
                case INITALIZED:
                    setStatus("待機中...", 0xFFFF00);
                    break;
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
     */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        animationTime += partialTick / 20f;

        // 背景描画
        guiGraphics.fill(0, 0, this.width, this.height, 0xC0101010);

        // タイトル描画
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // バージョン表示
        String version = "v" + SignPicture.MOD_VERSION;
        guiGraphics.drawString(this.font, version, this.width - this.font.width(version) - 5, 5, 0x808080);

        // プレビュー描画
        renderPreview(guiGraphics);

        // プロパティラベル描画
        int propX = previewX + PREVIEW_SIZE + 15;
        int propY = previewY;

        guiGraphics.drawString(this.font, "W", propX, propY + 4, 0xAAAAAA);
        guiGraphics.drawString(this.font, "H", propX + 65, propY + 4, 0xAAAAAA);

        propY += 22;
        guiGraphics.drawString(this.font, "X", propX, propY + 4, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Y", propX + 50, propY + 4, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Z", propX + 100, propY + 4, 0xAAAAAA);

        propY += 22;
        guiGraphics.drawString(this.font, "R", propX, propY + 4, 0xAAAAAA);

        // 情報表示
        int infoX = this.width / 2 + 60;
        int infoY = previewY;

        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        guiGraphics.drawString(this.font, "キャッシュ: " + contentCount, infoX, infoY, 0x888888);
        guiGraphics.drawString(this.font, "エントリ: " + entryCount, infoX, infoY + 12, 0x888888);

        // 視線先の情報表示
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
            if (mc.level != null && mc.level.getBlockEntity(pos) instanceof SignBlockEntity) {
                guiGraphics.drawString(this.font, "視線: 看板", infoX, infoY + 30, 0x55FF55);
            } else {
                guiGraphics.drawString(this.font, "視線: 他", infoX, infoY + 30, 0x888888);
            }
        } else {
            guiGraphics.drawString(this.font, "視線: なし", infoX, infoY + 30, 0x888888);
        }

        // ステータステキスト描画
        if (!statusText.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, statusText, this.width / 2, this.height - 45, statusColor);
        }

        // 入力フィールド描画
        for (EditBox field : allFields) {
            field.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        updateStatus();
    }

    /**
     * プレビュー領域を描画する。
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
                        dims += " (GIF)";
                    }
                    guiGraphics.drawCenteredString(this.font, dims, x + size / 2, y + size + 3, 0x888888);
                    return;
                }
            }

            // テクスチャがない場合は状態を表示
            StateType state = currentContent.getState().getType();
            String stateText = switch (state) {
                case INIT, INITALIZED -> "待機中...";
                case DOWNLOADING -> "DL中...";
                case LOADING -> "読込中...";
                case ERROR -> "エラー";
                default -> "...";
            };
            guiGraphics.drawCenteredString(this.font, stateText, x + size / 2, y + size / 2, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(this.font, "プレビュー", x + size / 2, y + size / 2 - 5, 0x666666);
            guiGraphics.drawCenteredString(this.font, "なし", x + size / 2, y + size / 2 + 5, 0x666666);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (EditBox field : allFields) {
            if (field.isFocused()) {
                return field.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (EditBox field : allFields) {
            if (field.isFocused()) {
                return field.charTyped(codePoint, modifiers);
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (EditBox field : allFields) {
            field.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * SignPicScreen画面を開く。
     */
    public static void open() {
        Minecraft.getInstance().setScreen(new SignPicScreen());
    }
}
