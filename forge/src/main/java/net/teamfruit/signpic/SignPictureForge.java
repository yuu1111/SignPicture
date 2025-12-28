package net.teamfruit.signpic;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
        SignPicture.initClient();
    }
}
