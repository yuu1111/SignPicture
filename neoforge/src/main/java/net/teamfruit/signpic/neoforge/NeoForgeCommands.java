package net.teamfruit.signpic.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.image.ImageLoader;

/**
 * NeoForge client command registration.
 */
public class NeoForgeCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("signpic")
                        .then(Commands.literal("clear")
                                .executes(ctx -> {
                                    clearCache();
                                    return 1;
                                }))
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    reloadTextures();
                                    return 1;
                                }))
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    showStatus();
                                    return 1;
                                }))
                        .executes(ctx -> {
                            showHelp();
                            return 1;
                        })
        );
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
