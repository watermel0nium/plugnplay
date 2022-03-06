package io.github.watermel0nium.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ServerWorldEvents {

    public static void init() {
        ServerTickEvents.START_WORLD_TICK.register((serverWorld) -> {

        });
    }
}
