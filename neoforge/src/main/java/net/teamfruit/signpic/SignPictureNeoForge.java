package net.teamfruit.signpic;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

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
        SignPicture.initClient();
    }
}
