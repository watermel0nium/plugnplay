package io.github.watermel0nium;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Pair;
import org.metafetish.buttplug.client.ButtplugClientDevice;
import org.metafetish.buttplug.client.ButtplugWSClient;
import org.metafetish.buttplug.core.messages.StopAllDevices;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

public class PlugClientManager {

    private static final int CONNECTION_ATTEMPTS = 5;
    private static PlugClientManager plugClientInstance = null;
    private volatile boolean running = false;

    private final ConcurrentHashMap<UUID, ButtplugWSClient> clients;
    private final BlockingQueue<Pair<ButtplugWSClient, ServerPlayerEntity>> pendingConnections;
    private final ScheduledExecutorService pendingConnectionsExecutor;

    private PlugClientManager() {
        clients = new ConcurrentHashMap<>();
        pendingConnections = new LinkedBlockingQueue<>();
        pendingConnectionsExecutor = Executors.newSingleThreadScheduledExecutor();
        pendingConnectionsExecutor.scheduleAtFixedRate(
                this::handlePendingConnections,
                0,
                20,
                TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
    }

    @Nullable
    public Collection<ButtplugClientDevice> getPlayerDevices(ServerPlayerEntity player) {
        ButtplugWSClient client = clients.get(player.getUuid());
        if(client != null) {
            var devices = client.getDevices();
            if(devices != null && !devices.isEmpty()) return devices;
        }
        return null;
    }

    public void connectClient(ServerPlayerEntity player) {
        try {
            ButtplugWSClient client = new ButtplugWSClient("plugnplay");
            player.sendMessage(new LiteralText("Attempting to connect..."), false);
            pendingConnections.add(new Pair<>(client, player));
        } catch(Exception e) {
            PlugnPlay.LOGGER.error(e.getMessage());
        }
    }

    public void disconnectClient(ServerPlayerEntity player) {
        if(clients.containsKey(player.getUuid())) {
            ButtplugWSClient client = clients.get(player.getUuid());
            if(client != null) {
                try {
                    client.sendMessage(new StopAllDevices());
                    client.Disconnect();
                } catch(Exception e) {
                    PlugnPlay.LOGGER.error("Failed to disconnect device for player: {} \n{}", player, e.getMessage());
                }
            }
            clients.remove(player.getUuid());
        }
    }

    private void handlePendingConnections() {
        try {
            var pendingConnection = pendingConnections.poll();
            if(pendingConnection != null) {
                var client = pendingConnection.getLeft();
                var player = pendingConnection.getRight();

                String ip = (player.getIp().equals("local")) ? "localhost" : player.getIp();
                client.Connect(new URI("ws://" + ip + ":12345/buttplug"), true);
                client.startScanning();

                Thread.sleep(2000);
                client.requestDeviceList();
                Thread.sleep(2000);

                var devices = client.getDevices();
                if(devices.isEmpty())
                    player.sendMessage(new LiteralText("Could not find any connected toys"), false);
                else {
                    String msg = "Found a toy: " + devices.get(0).getName();
                    player.sendMessage(new LiteralText(msg), false);
                }
                clients.put(player.getUuid(), client);
            }
        } catch (Exception e) {
            PlugnPlay.LOGGER.error(e.getMessage());
        }
    }

    public static PlugClientManager getInstance() {
        if(plugClientInstance == null) {
            plugClientInstance = new PlugClientManager();
        }
        return plugClientInstance;
    }

    private void onShutdown() {
        PlugnPlay.LOGGER.info("Disconnecting devices...");

        for (var client : clients.values()) {
            try {
                client.sendMessage(new StopAllDevices());
                client.Disconnect();
            } catch(Exception e) {
                PlugnPlay.LOGGER.error(e.getMessage());
            }
        }
        running = false;
        pendingConnectionsExecutor.shutdown();
    }
}
