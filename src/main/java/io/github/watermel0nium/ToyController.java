package io.github.watermel0nium;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.metafetish.buttplug.client.ButtplugClientDevice;
import org.metafetish.buttplug.client.ButtplugWSClient;
import org.metafetish.buttplug.core.messages.LinearCmd;
import org.metafetish.buttplug.core.messages.RotateCmd;
import org.metafetish.buttplug.core.messages.StopAllDevices;
import org.metafetish.buttplug.core.messages.VibrateCmd;

import static io.github.watermel0nium.config.ConfigHandler.config;

public class ToyController {
	private final Logger LOGGER = LogManager.getLogger();
	private final ButtplugWSClient CLIENT = new ButtplugWSClient(PlugnPlay.MOD_NAME);

	private ButtplugClientDevice device;
	private boolean shutDownHookAdded, isConnected;
	public String lastErrorMessage;
	public double currentVibrationLevel;
	private Class<?> CMD;

	public ToyController() {
		device = null;
		shutDownHookAdded = false;
		isConnected = false;
		lastErrorMessage = "";
		currentVibrationLevel = 0;
	}

	public boolean connectDevice() {
		try {
			device = null;
			CLIENT.Disconnect();

			CLIENT.Connect(new URI(config.serverURL), true);
			CLIENT.startScanning();

			CLIENT.setScanningFinished(() -> {
				PlugnPlay.LOGGER.info("Scanning finished :DDDD");
			});

			Thread.sleep(5000);
			CLIENT.requestDeviceList();

			LOGGER.info("Enumerating devices...");

			List<ButtplugClientDevice> devices = CLIENT.getDevices();

			int nDevices = devices.size();
			LOGGER.info(nDevices);

			if (nDevices < 1) {
				lastErrorMessage = "No device found";
			}

			for (var device : devices) {
				if (device.getAllowedMessages().containsKey(VibrateCmd.class.getSimpleName())
						|| device.getAllowedMessages().containsKey(LinearCmd.class.getSimpleName())) {
					LOGGER.info(device.getName());
					this.device = device;

					if (device.getAllowedMessages().containsKey(VibrateCmd.class.getSimpleName())) {
						CMD = VibrateCmd.class;
					} else if (device.getAllowedMessages().containsKey(LinearCmd.class.getSimpleName())) {
						CMD = LinearCmd.class;
					} else if (device.getAllowedMessages().containsKey(RotateCmd.class.getSimpleName())) {
						CMD = RotateCmd.class;
					}
					break;
				}
			}

			if (device == null) {
				LOGGER.warn("Couldn't find a compatible device");
				for (ButtplugClientDevice dev : devices) {
					LOGGER.warn("{}: {}", dev.getName(), dev.getAllowedMessages());
				}
			}

			if (Objects.nonNull(device) && !shutDownHookAdded) {
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						LOGGER.info("Disconnecting devices...");
						CLIENT.sendMessage(new StopAllDevices());
						CLIENT.Disconnect();
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

	public void setVibrationLevel(double level) {
		if (Objects.isNull(device))
			return;

		if (config.vibrate) {
			if (level == 0 && (CMD == VibrateCmd.class || CMD == RotateCmd.class)) {
				try {
					CLIENT.sendMessage(new StopAllDevices());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				_setVibrationLevel(level);
			}
		} else {
			if (currentVibrationLevel > 0) {
				_setVibrationLevel(level);
			}
		}
	}

	private void _setVibrationLevel(double level) {
		try {
			if (CMD == VibrateCmd.class) {
				var vibrateCmd = new VibrateCmd();
				vibrateCmd.setDeviceIndex(device.getIndex());
				vibrateCmd.setSpeeds(Collections.singletonList(new VibrateCmd.Speed(0, level)));
				CLIENT.sendDeviceMessage(device, vibrateCmd);
			} else if (CMD == LinearCmd.class) {
				var linearCmd = new LinearCmd();
				linearCmd.setDeviceIndex(device.getIndex());
				linearCmd.setVectors(Collections.singletonList(new LinearCmd.Vector(0, 0, level)));
				CLIENT.sendDeviceMessage(device, linearCmd);
			} else if (CMD == RotateCmd.class) {
				var rotateCmd = new RotateCmd();
				rotateCmd.setDeviceIndex(device.getIndex());
				RotateCmd.Rotation rot = new RotateCmd.Rotation(); // TODO fix this missing constructor in buttplug4j
				rot.setSpeed(level);
				rotateCmd.setRotations(Collections.singletonList(rot));
				CLIENT.sendDeviceMessage(device, rotateCmd);
			}
			currentVibrationLevel = level;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected() { return isConnected; }

	public String getDeviceName() {
		return (Objects.nonNull(device)) ? device.getName() : "<none>";
	}

	public long getDeviceId() {
		return (Objects.nonNull(device)) ? device.getIndex() : -1;
	}

	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

}
