package net.teamfruit.signpic.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.teamfruit.signpic.SignPicture;

import java.util.function.Consumer;

/**
 * Handles keybinding registration and processing.
 */
public class KeyBindingHandler {
    private static KeyBindingHandler instance;

    public static KeyBindingHandler getInstance() {
        if (instance == null) {
            instance = new KeyBindingHandler();
        }
        return instance;
    }

    private KeyMapping openGuiKey;
    private Consumer<Minecraft> guiOpener;

    private KeyBindingHandler() {}

    /**
     * Creates the keymappings. Must be called before registration.
     */
    public void createKeyMappings() {
        openGuiKey = new KeyMapping(
                "key.signpic.gui",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_BACKSLASH,
                "category.signpic"
        );
    }

    public KeyMapping getOpenGuiKey() {
        return openGuiKey;
    }

    /**
     * Sets the function to call when the GUI key is pressed.
     */
    public void setGuiOpener(Consumer<Minecraft> opener) {
        this.guiOpener = opener;
    }

    /**
     * Called each client tick to check for key presses.
     */
    public void onClientTick() {
        if (openGuiKey == null) {
            return;
        }

        while (openGuiKey.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && guiOpener != null) {
                SignPicture.LOGGER.debug("SignPicture GUI key pressed");
                guiOpener.accept(mc);
            }
        }
    }
}
