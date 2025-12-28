package net.teamfruit.signpic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.teamfruit.signpic.event.SignPicEvents;
import net.teamfruit.signpic.fabric.FabricCommands;
import net.teamfruit.signpic.fabric.VersionCompatFabric;
import net.teamfruit.signpic.gui.SignPicScreen;
import net.teamfruit.signpic.keybind.KeyBindingHandler;
import net.teamfruit.signpic.render.VersionCompat;

import java.nio.file.Path;

public class SignPictureFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        // Initialize version compatibility layer
        VersionCompat.init(new VersionCompatFabric());
        SignPicture.init();
    }

    @Override
    public void onInitializeClient() {
        // Set up cache directory
        Path cacheDir = FabricLoader.getInstance().getGameDir()
                .resolve("signpic")
                .resolve("cache");
        SignPicture.setCacheDirectory(cacheDir);

        // Initialize client
        SignPicture.initClient();

        // Register keybindings
        KeyBindingHandler keyHandler = KeyBindingHandler.getInstance();
        keyHandler.createKeyMappings();
        KeyBindingHelper.registerKeyBinding(keyHandler.getOpenGuiKey());
        keyHandler.setGuiOpener(mc -> {
            SignPicScreen.open();
        });

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SignPicture.onClientTick();
            keyHandler.onClientTick();
        });

        // Register client stopping event
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            SignPicEvents.onClientStopping();
        });

        // Register resource reload listener
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return VersionCompat.createModResourceLocation("reload_listener");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager manager) {
                        SignPicEvents.onResourceReload();
                    }
                }
        );

        // Register client commands
        ClientCommandRegistrationCallback.EVENT.register(FabricCommands::register);
    }
}
