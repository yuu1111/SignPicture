package net.teamfruit.signpic;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.teamfruit.signpic.event.SignPicEvents;
import net.teamfruit.signpic.gui.SignPicScreen;
import net.teamfruit.signpic.keybind.KeyBindingHandler;
import net.teamfruit.signpic.neoforge.NeoForgeCommands;
import net.teamfruit.signpic.neoforge.VersionCompatNeoForge;
import net.teamfruit.signpic.render.VersionCompat;

import java.nio.file.Path;

@Mod(SignPicture.MOD_ID)
public class SignPictureNeoForge {
    public SignPictureNeoForge(IEventBus modEventBus) {
        // Initialize version compatibility layer
        VersionCompat.init(new VersionCompatNeoForge());
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::onRegisterReloadListeners);
        modEventBus.addListener(this::onRegisterKeyMappings);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        SignPicture.init();
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        // Set up cache directory
        Path cacheDir = FMLPaths.GAMEDIR.get()
                .resolve("signpic")
                .resolve("cache");
        SignPicture.setCacheDirectory(cacheDir);

        // Initialize client
        SignPicture.initClient();

        // Register event handlers
        NeoForge.EVENT_BUS.register(new ClientEventHandler());
        NeoForge.EVENT_BUS.register(NeoForgeCommands.class);
    }

    private void onRegisterReloadListeners(final RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            SignPicEvents.onResourceReload();
        });
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        KeyBindingHandler keyHandler = KeyBindingHandler.getInstance();
        keyHandler.createKeyMappings();
        event.register(keyHandler.getOpenGuiKey());
        keyHandler.setGuiOpener(mc -> {
            SignPicScreen.open();
        });
    }

    public static class ClientEventHandler {
        @SubscribeEvent
        public void onClientTick(ClientTickEvent.Post event) {
            SignPicture.onClientTick();
            KeyBindingHandler.getInstance().onClientTick();
        }
    }
}
