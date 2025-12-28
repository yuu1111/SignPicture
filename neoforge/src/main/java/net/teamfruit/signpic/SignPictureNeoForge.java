package net.teamfruit.signpic;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.nio.file.Path;

@Mod(SignPicture.MOD_ID)
public class SignPictureNeoForge {
    public SignPictureNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);
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

        // Register client tick event
        NeoForge.EVENT_BUS.register(new ClientTickHandler());
    }

    public static class ClientTickHandler {
        @SubscribeEvent
        public void onClientTick(ClientTickEvent.Post event) {
            SignPicture.onClientTick();
        }
    }
}
