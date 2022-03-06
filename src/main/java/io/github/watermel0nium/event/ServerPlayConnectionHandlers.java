package io.github.watermel0nium.event;

import io.github.watermel0nium.PlugClientManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class ServerPlayConnectionHandlers {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionHandlers::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(ServerPlayConnectionHandlers::onPlayerDisconnect);
    }

    private static void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        PlugClientManager.getInstance().connectClient(handler.player);
    }

    private static void onPlayerDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        PlugClientManager.getInstance().disconnectClient(handler.player);
    }
}
