package net.teamfruit.signpic.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.teamfruit.signpic.SignPicture;
import net.teamfruit.signpic.content.ContentManager;
import net.teamfruit.signpic.entry.EntryManager;
import net.teamfruit.signpic.image.ImageLoader;

/**
 * SignPicture client-side commands.
 */
public class SignPicCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("signpic")
                        .then(Commands.literal("clear")
                                .executes(SignPicCommands::clearCache))
                        .then(Commands.literal("reload")
                                .executes(SignPicCommands::reloadTextures))
                        .then(Commands.literal("status")
                                .executes(SignPicCommands::showStatus))
                        .executes(SignPicCommands::showHelp)
        );
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal("§6[SignPicture] §fCommands:"));
        context.getSource().sendSystemMessage(Component.literal("§7/signpic clear §f- Clear all cached images"));
        context.getSource().sendSystemMessage(Component.literal("§7/signpic reload §f- Reload all textures"));
        context.getSource().sendSystemMessage(Component.literal("§7/signpic status §f- Show cache status"));
        return 1;
    }

    private static int clearCache(CommandContext<CommandSourceStack> context) {
        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        ImageLoader.clearAll();
        ContentManager.getInstance().clear();
        EntryManager.getInstance().clear();

        context.getSource().sendSystemMessage(
                Component.literal("§6[SignPicture] §fCleared " + contentCount + " cached images and " + entryCount + " entries")
        );
        SignPicture.LOGGER.info("Cache cleared: {} images, {} entries", contentCount, entryCount);
        return 1;
    }

    private static int reloadTextures(CommandContext<CommandSourceStack> context) {
        ImageLoader.clearAll();
        ContentManager.getInstance().clearTextures();

        context.getSource().sendSystemMessage(
                Component.literal("§6[SignPicture] §fReloading all textures...")
        );
        SignPicture.LOGGER.info("Texture reload triggered");
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) {
        int contentCount = ContentManager.getInstance().getContentCount();
        int entryCount = EntryManager.getInstance().getEntryCount();

        context.getSource().sendSystemMessage(Component.literal("§6[SignPicture] §fStatus:"));
        context.getSource().sendSystemMessage(Component.literal("§7  Cached images: §f" + contentCount));
        context.getSource().sendSystemMessage(Component.literal("§7  Active entries: §f" + entryCount));
        context.getSource().sendSystemMessage(Component.literal("§7  Version: §f" + SignPicture.MOD_VERSION));
        return 1;
    }
}
