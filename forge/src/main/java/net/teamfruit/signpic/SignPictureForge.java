package net.teamfruit.signpic;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

@Mod(SignPicture.MOD_ID)
public class SignPictureForge {
    public SignPictureForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
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
        MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
    }

    public static class ClientTickHandler {
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                SignPicture.onClientTick();
            }
        }
    }
}
