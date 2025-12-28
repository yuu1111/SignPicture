package net.teamfruit.signpic;

import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.teamfruit.signpic.event.SignPicEvents;
import net.teamfruit.signpic.forge.ForgeCommands;
import net.teamfruit.signpic.forge.VersionCompatForge;
import net.teamfruit.signpic.gui.SignPicScreen;
import net.teamfruit.signpic.keybind.KeyBindingHandler;
import net.teamfruit.signpic.render.VersionCompat;

import java.nio.file.Path;

@Mod(SignPicture.MOD_ID)
public class SignPictureForge {
    public SignPictureForge() {
        // Initialize version compatibility layer
        VersionCompat.init(new VersionCompatForge());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterReloadListeners);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterKeyMappings);
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
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
        MinecraftForge.EVENT_BUS.register(ForgeCommands.class);
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
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                SignPicture.onClientTick();
                KeyBindingHandler.getInstance().onClientTick();
            }
        }
    }
}
