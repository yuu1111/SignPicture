package net.teamfruit.signpic.config;

import net.teamfruit.signpic.SignPicture;

/**
 * SignPicture configuration holder.
 * Platform-specific implementations handle actual config loading/saving.
 */
public class SignPicConfig {
    private static SignPicConfig instance;

    public static SignPicConfig get() {
        if (instance == null) {
            instance = new SignPicConfig();
        }
        return instance;
    }

    // Image settings
    public int imageWidthLimit = 512;
    public int imageHeightLimit = 512;
    public boolean imageFastResize = false;
    public boolean imageAnimateGif = true;

    // HTTP settings
    public int httpThreads = 3;
    public int httpTimeout = 15000;

    // Content settings
    public int contentLoadThreads = 3;
    public int contentMaxBytes = 32 * 1024 * 1024;
    public int contentGcDelayTicks = 15 * 20;
    public int contentMaxRetry = 3;

    // Entry settings
    public int entryGcDelayTicks = 15 * 20;

    // Render settings
    public boolean renderOverlayPanel = true;
    public boolean renderGuiOverlay = true;
    public boolean renderUseMipmap = true;
    public boolean renderMipmapNearest = false;
    public double renderViewOpacity = 0.5;
    public double renderPreviewFixedOpacity = 0.7;
    public double renderPreviewFloatedOpacity = 0.49;

    // ChatPicture settings
    public boolean chatpicEnable = true;
    public int chatpicLines = 4;
    public int chatpicStackTicks = 50;

    // API settings
    public String apiUploaderType = "";
    public String apiUploaderKey = "";
    public String apiShortenerType = "";
    public String apiShortenerKey = "";

    // Multiplay settings
    public boolean multiplayPaas = true;
    public int multiplayPaasMinEditTime = 150;
    public int multiplayPaasMinLineTime = 50;
    public int multiplayPaasMinCharTime = 50;

    // Version settings
    public boolean versionNotice = true;
    public boolean versionUpdateGui = true;

    // Debug
    public boolean debugLog = false;

    private SignPicConfig() {}

    public void load() {
        SignPicture.LOGGER.debug("Loading SignPicture config");
    }

    public void save() {
        SignPicture.LOGGER.debug("Saving SignPicture config");
    }
}
