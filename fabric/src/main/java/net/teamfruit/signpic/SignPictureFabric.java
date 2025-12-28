package net.teamfruit.signpic;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class SignPictureFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        SignPicture.init();
    }

    @Override
    public void onInitializeClient() {
        SignPicture.initClient();
    }
}
