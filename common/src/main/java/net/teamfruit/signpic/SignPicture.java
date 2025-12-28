package net.teamfruit.signpic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignPicture {
    public static final String MOD_ID = "signpic";
    public static final String MOD_NAME = "SignPicture";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        LOGGER.info("Initializing {}", MOD_NAME);
    }

    public static void initClient() {
        LOGGER.info("Initializing {} client", MOD_NAME);
    }
}
