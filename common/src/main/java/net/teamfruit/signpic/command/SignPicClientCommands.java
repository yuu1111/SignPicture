package net.teamfruit.signpic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.image.ImageLoader;

/**
 * SignPicture client-side commands (for Fabric's client command system).
 */
public class SignPicClientCommands {

    /**
     * Register commands for Fabric client command system.
     */
    public static <S> void register(CommandDispatcher<S> dispatcher, CommandBuildContext registryAccess) {
        // Use Fabric's FabricClientCommandSource
        registerCommands(dispatcher);
    }

    @SuppressWarnings("unchecked")
    private static <S> void registerCommands(CommandDispatcher<S> dispatcher) {
        try {
            // Import at runtime to avoid class loading issues
            Class<?> commandsClass = Class.forName("net.fabricmc.fabric.api.client.command.v2.ClientCommandManager");
            java.lang.reflect.Method literalMethod = commandsClass.getMethod("literal", String.class);

            Object signpicLiteral = literalMethod.invoke(null, "signpic");

            // Build command tree using reflection for Fabric compatibility
            var builder = (com.mojang.brigadier.builder.LiteralArgumentBuilder<S>) signpicLiteral;

            builder.then(
                    ((com.mojang.brigadier.builder.LiteralArgumentBuilder<S>) literalMethod.invoke(null, "clear"))
                            .executes(ctx -> {
                                clearCache();
                                return 1;
                            })
            ).then(
                    ((com.mojang.brigadier.builder.LiteralArgumentBuilder<S>) literalMethod.invoke(null, "reload"))
                            .executes(ctx -> {
                                reloadTextures();
                                return 1;
                            })
            ).then(
                    ((com.mojang.brigadier.builder.LiteralArgumentBuilder<S>) literalMethod.invoke(null, "status"))
                            .executes(ctx -> {
                                showStatus();
                                return 1;
                            })
            ).executes(ctx -> {
                showHelp();
                return 1;
            });

            dispatcher.register(builder);
        } catch (Exception e) {
            SignPicture.LOGGER.error("Failed to register client commands", e);
        }
    }

    private static void sendMessage(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal(message));
        }
    }

    private static void showHelp() {
        sendMessage("§6[SignPicture] §fCommands:");
        sendMessage("§7/signpic clear §f- Clear all cached images");
        sendMessage("§7/signpic reload §f- Reload all textures");
        sendMessage("§7/signpic status §f- Show cache status");
    }

    private static void clearCache() {
        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        ImageLoader.clearAll();
        ContentManager.getInstance().clear();
        EntryManager.getInstance().clear();

        sendMessage("§6[SignPicture] §fCleared " + contentCount + " cached images and " + entryCount + " entries");
        SignPicture.LOGGER.info("Cache cleared: {} images, {} entries", contentCount, entryCount);
    }

    private static void reloadTextures() {
        ImageLoader.clearAll();
        ContentManager.getInstance().clearTextures();

        sendMessage("§6[SignPicture] §fReloading all textures...");
        SignPicture.LOGGER.info("Texture reload triggered");
    }

    private static void showStatus() {
        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        sendMessage("§6[SignPicture] §fStatus:");
        sendMessage("§7  Cached images: §f" + contentCount);
        sendMessage("§7  Active entries: §f" + entryCount);
        sendMessage("§7  Version: §f" + SignPicture.MOD_VERSION);
    }
}
