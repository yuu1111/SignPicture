package net.teamfruit.signpic.event;

import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.image.ImageLoader;

/**
 * Central event handler for SignPicture.
 * Platform-specific code should call these methods from their event handlers.
 */
public class SignPicEvents {

    /**
     * Called when resources are reloaded (e.g., resource pack change).
     * Clears all cached textures.
     */
    public static void onResourceReload() {
        SignPicture.LOGGER.info("Resources reloaded, clearing texture cache");
        ImageLoader.clearAll();
        ContentManager.getInstance().clearTextures();
    }

    /**
     * Called when the client disconnects from a world.
     * Clears cached content to free memory.
     */
    public static void onWorldUnload() {
        SignPicture.LOGGER.debug("World unloaded, clearing entry cache");
        EntryManager.getInstance().clear();
    }

    /**
     * Called when the client is shutting down.
     */
    public static void onClientStopping() {
        SignPicture.shutdown();
    }
}
