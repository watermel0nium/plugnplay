package com.therainbowville.minegasm.client;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.metafetish.buttplug.client.ButtplugClientDevice;
import org.metafetish.buttplug.client.ButtplugWSClient;
import org.metafetish.buttplug.core.messages.StopAllDevices;
import org.metafetish.buttplug.core.messages.VibrateCmd;

import com.therainbowville.minegasm.config.MinegasmConfig;

public class ToyController {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ButtplugWSClient client = new ButtplugWSClient("Minegasm");
	private static ButtplugClientDevice device = null;
	private static boolean shutDownHookAdded = false;
	public static String lastErrorMessage = "";
	public static boolean isConnected = false;
	public static double currentVibrationLevel = 0;

	public static boolean connectDevice() {
		try {
			device = null;
			client.Disconnect();
			LOGGER.info("URL: " + MinegasmConfig.INSTANCE.serverUrl);

			client.Connect(new URI(MinegasmConfig.INSTANCE.serverUrl), true);
			client.startScanning();

			Thread.sleep(5000);
			client.requestDeviceList();

			LOGGER.info("Enumerating devices...");

			List<ButtplugClientDevice> devices = client.getDevices();

			int nDevices = devices.size();
			LOGGER.info(nDevices);

			if (nDevices < 1) {
				lastErrorMessage = "No device found";
			}

			for (ButtplugClientDevice dev : devices) {
				if (dev.getAllowedMessages().keySet().contains(VibrateCmd.class.getSimpleName())) {
					LOGGER.info(dev.getName());
					device = dev;
					try {
						VibrateCmd cmd = new VibrateCmd();
						cmd.setDeviceIndex(device.getIndex());
						client.sendDeviceMessage(device, cmd);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}

			if (Objects.nonNull(device) && !shutDownHookAdded) {
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						LOGGER.info("Disconnecting devices...");
						client.sendMessage(new StopAllDevices());
						client.Disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}));

				shutDownHookAdded = true;
			}

			isConnected = true;
		} catch (Exception e) {
			lastErrorMessage = e.getMessage();
			e.printStackTrace();
		}

		return Objects.nonNull(device);
	}

	public static void setVibrationLevel(double level) {
		if (Objects.isNull(device))
			return;

		if (MinegasmConfig.INSTANCE.vibrate) {
			try {
				if (level == 0) {
					client.sendMessage(new StopAllDevices());
				} else {
					VibrateCmd cmd = new VibrateCmd();
					cmd.setDeviceIndex(device.getIndex());
					cmd.setSpeeds(Collections.singletonList(new VibrateCmd.Speed(0, level)));
					client.sendDeviceMessage(device, cmd);
					currentVibrationLevel = level;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (currentVibrationLevel > 0) {
				try {
					level = 0;
					VibrateCmd cmd = new VibrateCmd();
					cmd.setDeviceIndex(device.getIndex());
					cmd.setSpeeds(Collections.singletonList(new VibrateCmd.Speed(0, level)));
					client.sendDeviceMessage(device, cmd);
					currentVibrationLevel = level;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getDeviceName() {
		return (Objects.nonNull(device)) ? device.getName() : "<none>";
	}

	public static long getDeviceId() {
		return (Objects.nonNull(device)) ? device.getIndex() : -1;
	}

	public static String getLastErrorMessage() {
		return lastErrorMessage;
	}
}