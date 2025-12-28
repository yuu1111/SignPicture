package net.teamfruit.signpic;

import net.teamfruit.signpic.config.SignPicConfig;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.entry.EntryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SignPicture {
    public static final String MOD_ID = "signpic";
    public static final String MOD_NAME = "SignPicture";
    public static final String MOD_VERSION = "3.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static boolean initialized = false;
    private static boolean clientInitialized = false;
    private static int tickCounter = 0;

    public static void init() {
        if (initialized) return;
        initialized = true;

        LOGGER.info("Initializing {} v{}", MOD_NAME, MOD_VERSION);
        SignPicConfig.get().load();
    }

    public static void initClient() {
        if (clientInitialized) return;
        clientInitialized = true;

        LOGGER.info("Initializing {} client", MOD_NAME);
    }

    public static void setCacheDirectory(Path directory) {
        ContentManager.setCacheDirectory(directory);
        LOGGER.info("Cache directory set to: {}", directory);
    }

    public static void onClientTick() {
        tickCounter++;

        // Run GC periodically
        int gcInterval = SignPicConfig.get().contentGcDelayTicks;
        if (gcInterval > 0 && tickCounter % gcInterval == 0) {
            EntryManager.getInstance().gc();
            ContentManager.getInstance().gc();
        }
    }

    public static void shutdown() {
        LOGGER.info("Shutting down {}", MOD_NAME);
        ContentManager.getInstance().clear();
        EntryManager.getInstance().clear();
    }
}
