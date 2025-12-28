package net.teamfruit.signpic.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.image.ImageLoader;

/**
 * Main SignPicture GUI screen.
 */
public class SignPicScreen extends Screen {
    private EditBox urlField;
    private String statusText = "";

    public SignPicScreen() {
        super(Component.literal("SignPicture"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // URL input field
        this.urlField = new EditBox(this.font, centerX - 150, centerY - 60, 300, 20, Component.literal("URL"));
        this.urlField.setMaxLength(2048);
        this.urlField.setHint(Component.literal("Enter image URL..."));
        this.addWidget(this.urlField);

        // Load button
        this.addRenderableWidget(Button.builder(Component.literal("Load Image"), button -> {
            loadImage();
        }).bounds(centerX - 150, centerY - 30, 95, 20).build());

        // Clear cache button
        this.addRenderableWidget(Button.builder(Component.literal("Clear Cache"), button -> {
            clearCache();
        }).bounds(centerX - 50, centerY - 30, 95, 20).build());

        // Reload button
        this.addRenderableWidget(Button.builder(Component.literal("Reload"), button -> {
            reloadTextures();
        }).bounds(centerX + 50, centerY - 30, 95, 20).build());

        // Close button
        this.addRenderableWidget(Button.builder(Component.literal("Close"), button -> {
            this.onClose();
        }).bounds(centerX - 50, centerY + 50, 100, 20).build());

        updateStatus();
    }

    private void loadImage() {
        String url = urlField.getValue().trim();
        if (!url.isEmpty()) {
            ContentManager.getInstance().getOrCreate(url);
            statusText = "Loading: " + url;
            SignPicture.LOGGER.info("Loading image from GUI: {}", url);
        }
    }

    private void clearCache() {
        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        ImageLoader.clearAll();
        ContentManager.getInstance().clear();
        EntryManager.getInstance().clear();

        statusText = "Cleared " + contentCount + " images, " + entryCount + " entries";
        SignPicture.LOGGER.info("Cache cleared from GUI");
        updateStatus();
    }

    private void reloadTextures() {
        ImageLoader.clearAll();
        ContentManager.getInstance().clearTextures();
        statusText = "Reloading textures...";
        SignPicture.LOGGER.info("Texture reload triggered from GUI");
    }

    private void updateStatus() {
        // Status is updated in render
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw semi-transparent background
        guiGraphics.fill(0, 0, this.width, this.height, 0xC0101010);

        // Draw title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Draw version
        String version = "v" + SignPicture.MOD_VERSION;
        guiGraphics.drawString(this.font, version, this.width - this.font.width(version) - 5, 5, 0x808080);

        // Draw status info
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        guiGraphics.drawCenteredString(this.font, "Cached Images: " + contentCount, centerX, centerY + 10, 0xAAAAAA);
        guiGraphics.drawCenteredString(this.font, "Active Entries: " + entryCount, centerX, centerY + 25, 0xAAAAAA);

        // Draw status message
        if (!statusText.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, statusText, centerX, centerY + 80, 0xFFFF00);
        }

        // Draw URL field
        this.urlField.render(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        // EditBox doesn't need explicit tick in modern versions
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.urlField.isFocused()) {
            return this.urlField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.urlField.isFocused()) {
            return this.urlField.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.urlField.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Opens the SignPicture screen.
     */
    public static void open() {
        Minecraft.getInstance().setScreen(new SignPicScreen());
    }
}
