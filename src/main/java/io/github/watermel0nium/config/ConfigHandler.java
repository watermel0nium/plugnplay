package io.github.watermel0nium.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import io.github.watermel0nium.PlugnPlay;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigHandler {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	public static ConfigFile config;

	public static void load() {
		if(!getConfigFile().exists()) config = new ConfigFile();
		else {
			try (FileInputStream in = new FileInputStream(getConfigFile())) {
				config = MAPPER.readValue(in, ConfigFile.class);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config.setMode(config.mode);
	}

	public static void save() {
		try (FileOutputStream out = new FileOutputStream(getConfigFile())) {
			MAPPER.writeValue(out, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File getConfigFile() {
		return FabricLoader.getInstance().getConfigDir().resolve(PlugnPlay.MOD_ID + ".json").toFile();
	}

	@Override
	public String toString() {
		return "MinegasmConfig [serverUrl="
				+ config.serverURL
				+ ", vibrate="
				+ config.vibrate
				+ ", mode="
				+ config.mode
				+ ", attackIntensity="
				+ config.attackIntensity
				+ ", hurtIntensity="
				+ config.hurtIntensity
				+ ", mineIntensity="
				+ config.mineIntensity
				+ ", xpChangeIntensity="
				+ config.xpChangeIntensity
				+ ", harvestIntensity="
				+ config.harvestIntensity
				+ ", vitalityIntensity="
				+ config.vitalityIntensity
				+ ", sprintIntensity="
				+ config.sprintingIntensity
				+ ", placeIntensity="
				+ config.placeBlockIntensity
				+ "]";
	}

	public record Intensities(int attack, int hurt, int mine, int xpChange, int harvest, int vitality, int sprinting, int placeBlock) {
		public static final Intensities MASOCHIST, HEDONIST, NORMAL;

		static {
			MASOCHIST = new Intensities(0, 100, 0, 0, 0, 10, 0, 0);
			HEDONIST = new Intensities(60, 10, 80, 100, 20, 10, 10, 40);
			NORMAL = new Intensities(40, 50, 0, 100, 0, 0, 0, 0);
		}
	}

	public static class ConfigFile {
		public String serverURL;
		public boolean vibrate;
		private GameplayMode mode;
		public int attackIntensity, hurtIntensity, mineIntensity, xpChangeIntensity, harvestIntensity, vitalityIntensity, sprintingIntensity, placeBlockIntensity;

		public ConfigFile() {
			this.serverURL = "ws://localhost:12345/buttplug";
			this.vibrate = true;
			this.mode = GameplayMode.NORMAL;
			setIntensities(Intensities.NORMAL);
		}

		public void setIntensities(Intensities intensities) {
			attackIntensity = intensities.attack();
			hurtIntensity = intensities.hurt();
			mineIntensity = intensities.mine();
			xpChangeIntensity = intensities.xpChange();
			harvestIntensity = intensities.harvest();
			vitalityIntensity = intensities.vitality();
			sprintingIntensity = intensities.sprinting();
			placeBlockIntensity = intensities.placeBlock();
		}

		void setMode(GameplayMode mode) {
			this.mode = mode;
			switch(mode) {
				case NORMAL -> setIntensities(Intensities.NORMAL);
				case MASOCHIST -> setIntensities(Intensities.MASOCHIST);
				case HEDONIST -> setIntensities(Intensities.HEDONIST);
			}
		}
		public GameplayMode getMode() { return this.mode; }
	}
}
