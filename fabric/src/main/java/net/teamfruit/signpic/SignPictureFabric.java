package net.teamfruit.signpic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class SignPictureFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
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

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            SignPicture.onClientTick();
        });
    }
}
