package net.teamfruit.signpic.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.image.ImageLoader;

/**
 * Fabric-specific client command registration.
 */
public class FabricCommands {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
                ClientCommandManager.literal("signpic")
                        .then(ClientCommandManager.literal("clear")
                                .executes(ctx -> {
                                    clearCache(ctx.getSource());
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("reload")
                                .executes(ctx -> {
                                    reloadTextures(ctx.getSource());
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("status")
                                .executes(ctx -> {
                                    showStatus(ctx.getSource());
                                    return 1;
                                }))
                        .executes(ctx -> {
                            showHelp(ctx.getSource());
                            return 1;
                        })
        );
    }

    private static void showHelp(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("§6[SignPicture] §fCommands:"));
        source.sendFeedback(Component.literal("§7/signpic clear §f- Clear all cached images"));
        source.sendFeedback(Component.literal("§7/signpic reload §f- Reload all textures"));
        source.sendFeedback(Component.literal("§7/signpic status §f- Show cache status"));
    }

    private static void clearCache(FabricClientCommandSource source) {
        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        ImageLoader.clearAll();
        ContentManager.getInstance().clear();
        EntryManager.getInstance().clear();

        source.sendFeedback(
                Component.literal("§6[SignPicture] §fCleared " + contentCount + " cached images and " + entryCount + " entries")
        );
        SignPicture.LOGGER.info("Cache cleared: {} images, {} entries", contentCount, entryCount);
    }

    private static void reloadTextures(FabricClientCommandSource source) {
        ImageLoader.clearAll();
        ContentManager.getInstance().clearTextures();

        source.sendFeedback(Component.literal("§6[SignPicture] §fReloading all textures..."));
        SignPicture.LOGGER.info("Texture reload triggered");
    }

    private static void showStatus(FabricClientCommandSource source) {
        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        source.sendFeedback(Component.literal("§6[SignPicture] §fStatus:"));
        source.sendFeedback(Component.literal("§7  Cached images: §f" + contentCount));
        source.sendFeedback(Component.literal("§7  Active entries: §f" + entryCount));
        source.sendFeedback(Component.literal("§7  Version: §f" + SignPicture.MOD_VERSION));
    }
}
