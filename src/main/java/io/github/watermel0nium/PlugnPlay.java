package io.github.watermel0nium;

import io.github.watermel0nium.event.ServerPlayConnectionHandlers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.ModInitializer;

public class PlugnPlay implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger(PlugnPlay.class);

    public static final String MOD_ID = "plugnplay";
    public static final String MOD_NAME = "Plug n' Play";

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        PlugClientManager.getInstance();
        ServerPlayConnectionHandlers.init();
    }
}
