package io.github.watermel0nium;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.watermel0nium.config.ConfigHandler;

import java.util.HashMap;
import java.util.Map;


public class PlugnPlay implements ClientModInitializer {

	public static Logger LOGGER = LogManager.getLogger(PlugnPlay.class);

	public static final String MOD_ID = "plugnplay";
	public static final String MOD_NAME = "Plug n' Play";

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing");
		ConfigHandler.load();
		ClientTickEvents.END_CLIENT_TICK.register((client) -> ClientEventHandler.onClientTick());
	}
}
