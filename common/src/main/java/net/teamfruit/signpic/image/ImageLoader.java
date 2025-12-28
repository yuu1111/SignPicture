package net.teamfruit.signpic.image;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.Content;
import net.teamfruit.signpic.content.ContentTexture;
import net.teamfruit.signpic.render.VersionCompat;
import net.teamfruit.signpic.state.State;
import net.teamfruit.signpic.state.StateType;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads images and registers them with Minecraft's texture system.
 */
public class ImageLoader {
    private static final AtomicInteger TEXTURE_COUNTER = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, ResourceLocation> registeredTextures = new ConcurrentHashMap<>();

    /**
     * Loads an image from the given path and creates a ContentTexture.
     * Must be called from the render thread.
     */
    @Nullable
    public static ContentTexture loadImage(Path imagePath, State state) {
        try {
            state.setType(StateType.LOADING);
            state.setMessage("Loading image...");

            // Load the image
            NativeImage image;
            try (InputStream is = new FileInputStream(imagePath.toFile())) {
                image = NativeImage.read(is);
            }

            if (image == null) {
                state.setError(new RuntimeException("Failed to decode image"));
                return null;
            }

            int width = image.getWidth();
            int height = image.getHeight();

            // Create a unique texture ID for this image
            String textureKey = generateTextureKey(imagePath.toString());
            ResourceLocation textureLocation = getOrCreateTextureLocation(textureKey);

            // Register the texture with Minecraft
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            Minecraft.getInstance().getTextureManager().register(textureLocation, dynamicTexture);

            // Store the mapping
            registeredTextures.put(textureKey, textureLocation);

            // Create frame (single frame for static images)
            List<ContentTexture.Frame> frames = new ArrayList<>();
            frames.add(new ContentTexture.Frame(textureLocation, 0));

            state.setType(StateType.LOADED);
            state.setMessage(null);

            SignPicture.LOGGER.info("Loaded image: {}x{} from {}", width, height, imagePath);

            return new ContentTexture(frames, width, height);
        } catch (Exception e) {
            SignPicture.LOGGER.error("Failed to load image: {}", imagePath, e);
            state.setError(e);
            return null;
        }
    }

    /**
     * Schedules image loading on the main thread.
     */
    public static void scheduleLoad(Content content) {
        Path cachedFile = content.getCachedFile();
        if (cachedFile == null || !cachedFile.toFile().exists()) {
            return;
        }

        // Schedule loading on the render thread
        Minecraft.getInstance().execute(() -> {
            ContentTexture texture = loadImage(cachedFile, content.getState());
            if (texture != null) {
                content.setTexture(texture);
            }
        });
    }

    private static String generateTextureKey(String path) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(path.getBytes());
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

    private static ResourceLocation getOrCreateTextureLocation(String key) {
        ResourceLocation existing = registeredTextures.get(key);
        if (existing != null) {
            return existing;
        }
        return VersionCompat.createModResourceLocation("dynamic/" + key);
    }

    /**
     * Unregisters a texture from Minecraft's texture manager.
     */
    public static void unregisterTexture(ResourceLocation location) {
        if (location != null) {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().getTextureManager().release(location);
            });
        }
    }

    /**
     * Clears all registered dynamic textures.
     */
    public static void clearAll() {
        for (ResourceLocation location : registeredTextures.values()) {
            Minecraft.getInstance().getTextureManager().release(location);
        }
        registeredTextures.clear();
    }
}
