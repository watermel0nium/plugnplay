package io.github.watermel0nium;

import java.util.Arrays;
import java.util.UUID;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemPlacementContext;
import org.apache.logging.log4j.LogManager;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import static io.github.watermel0nium.config.ConfigHandler.config;

public class ClientEventHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int TICKS_PER_SECOND = 20;
	private static final double[] STATE = new double[1200];

	private static String playerName;
	private static UUID playerID;
	private static int tickCounter, clientTickCounter;
	private static boolean paused;

	private static final ToyController toyController = new ToyController();

	private static void clearState() {
		playerName = null;
		playerID = null;
		tickCounter = -1;
		clientTickCounter = -1;
		Arrays.fill(STATE, 0);
		paused = false;
	}

	private static int getStateCounter() {
		return tickCounter / 20;
	}

	private static void setState(int start, int duration, int intensity, boolean decay) {
		if (duration <= 0) {
			return;
		}

		if (decay) {
			int safeDuration = Math.max(0, duration - 2);
			for (int i = 0; i < safeDuration; i++) {
				setState(start + i, intensity);
			}
			setState(start + safeDuration, intensity / 2);
			setState(start + safeDuration + 1, intensity / 4);
		} else {
			for (int i = 0; i < duration; i++) {
				setState(start + i, intensity);
			}
		}
	}

	private static void setState(int counter, int intensity) {
		boolean accumulate = false; // XXX reserved for future use
		setState(counter, intensity, accumulate);
	}

	private static void setState(int counter, int intensity, boolean accumulate) {
		int safeCounter = counter % STATE.length;
		if (accumulate) {
			STATE[safeCounter] = Math.min(1.0, STATE[safeCounter] + (intensity / 100.0));
		} else {
			STATE[safeCounter] = Math.min(1.0, Math.max(STATE[safeCounter], (intensity / 100.0)));
		}
	}

	public static void onAttack(PlayerEntity player, Entity target) {
		GameProfile profile = player.getGameProfile();

		if(profile.getId().equals(playerID)) {
			setState(getStateCounter(), 3, config.attackIntensity, true);
		}
	}

	private static int vitality = 0;

	public static void onHurt(GameProfile profile, DamageSource source, float amount) {
		if (profile.getId().equals(playerID)) {

			vitality -= amount;
			int vitalityIntensity = 0;
			if (vitality < 20 && vitality > 0) {
				vitalityIntensity = config.vitalityIntensity;
				vitalityIntensity = (int) ((vitalityIntensity / 100f) * vitalityIntensity * (1 - vitality / 20f));
			}

			//set intensity strength depending on damage.
			int damageIntensity = config.hurtIntensity;
			if (!source.isExplosive()) {
				amount /= 4;
				if (amount > 0.9f) {
					damageIntensity = (int) (0.9f * damageIntensity);
				}
				damageIntensity = (int) ((amount * 0.9f) * damageIntensity);
			}
			int stateCounter = getStateCounter();
			if (vitalityIntensity > damageIntensity) {
				setState(stateCounter, vitalityIntensity);
			} else {
				setState(stateCounter, 3, damageIntensity, true);
			}

		}
	}

	public static void onPlayerTick(PlayerEntity player) {
		GameProfile profile = player.getGameProfile();

		vitality = (int) player.getHealth();
		//float playerFoodLevel = player.getHungerManager().getFoodLevel();

		tickCounter = (tickCounter + 1) % (20 * (60 * TICKS_PER_SECOND)); // 20 min day cycle

		if (tickCounter % TICKS_PER_SECOND == 0) { // every 1 sec
			if (profile.getId().equals(playerID)) {
				int stateCounter = getStateCounter();

				//set vibration based on hp% as a pow2 curve
				if (vitality < 20 && vitality > 0) {
					int intensity = config.vitalityIntensity;
					if (intensity > 0) {
						setState(stateCounter, (int) ((intensity / 100f) * intensity * (1 - vitality / 20f)));
					}
				}

				double newVibrationLevel = STATE[stateCounter];
				STATE[stateCounter] = 0;

				LOGGER.trace("Tick " + stateCounter + ": " + newVibrationLevel);

				toyController.setVibrationLevel(newVibrationLevel);
			}
		}
		if (isSprinting && (tickCounter % TICKS_PER_SECOND) == 0) {
			setSprintVibrations();
		}

		//if (tickCounter % (5 * TICKS_PER_SECOND) == 0) { // 5 secs
		//	LOGGER.debug("Health: " + playerHealth);
		//	LOGGER.debug("Food: " + playerFoodLevel);
		//}
	}

	public static void onClientTick() {
		if (tickCounter >= 0) {
			if (tickCounter != clientTickCounter) {
				clientTickCounter = tickCounter;
				paused = false;
			} else {
				if (!paused) {
					paused = true;
					LOGGER.debug("Pausing");
					toyController.setVibrationLevel(0);
				}
				if (paused) {
					LOGGER.trace("Paused");
				}
			}
		}
	}

	private static void populatePlayerInfo() {
		GameProfile profile = MinecraftClient.getInstance().getSession().getProfile();
		playerName = profile.getName();
		playerID = profile.getId();
		LOGGER.info("Current player: " + playerName + " " + playerID.toString());
	}

	public static void onWorldLoaded(World world) {
		LOGGER.info("World loaded: " + world.toString());

		populatePlayerInfo();
	}

	public static void onDeath(PlayerEntity player) {
		GameProfile profile = player.getGameProfile();

		if (profile.getId().equals(playerID)) {
			toyController.setVibrationLevel(0);
		}
	}

	public static void onHarvest(PlayerEntity player, BlockState blockState, boolean canHarvest) {
		GameProfile profile = player.getGameProfile();

		if (profile.getId().equals(playerID)) {
			Block block = blockState.getBlock();
			// ToolType. AXE, HOE, PICKAXE, SHOVEL

			float blockHardness = block.getHardness();
			// LOGGER.debug("Harvest: tool: "
			// + block.getHarvestTool(blockState)
			// + " can harvest? "
			// + event.canHarvest()
			// + " hardness: "
			// + blockHardness);

			int intensity
					= Math.toIntExact(Math.round((config.harvestIntensity * (blockHardness / 50f))));

			if (canHarvest) {
				setState(getStateCounter(), 2, intensity, false);
			}
		}
	}

	public static void onBreak(PlayerEntity player, BlockState blockState) {
		GameProfile profile = player.getGameProfile();

		if (profile.getId().equals(playerID)) {
			Block block = blockState.getBlock();

			float blockHardness = block.getDefaultState().getHardness(null, null);

			boolean usingAppropriateTool = player.canHarvest(blockState);

			if (usingAppropriateTool) {
				int duration = Math.max(1,
						Math.min(5, Math.toIntExact(Math.round(Math.ceil(Math.log(blockHardness + 0.5))))));
				int intensity
						= Math.toIntExact(Math.round((config.mineIntensity / 100.0 * (blockHardness / 50.0)) * 100));
				setState(getStateCounter(), duration, intensity, true);
			}
		}
	}

	public static void onRespawn() {
		clearState();
		toyController.setVibrationLevel(0);
		populatePlayerInfo();
	}

	public static void onWorldExit(Entity entity) {
		if ((entity instanceof PlayerEntity) && (playerName != null)) {
			clearState();
		}
	}

	public static void onWorldEntry(Entity entity) {
		if (entity instanceof ClientPlayerEntity) {
			LOGGER.info("Entered world: " + entity);

			if (playerName != null) {
				PlayerEntity player = (PlayerEntity) entity;
				GameProfile profile = player.getGameProfile();

				if (profile.getId().equals(playerID)) {
					LOGGER.info("Player in: " + playerName + " " + playerID);

					if (!toyController.isConnected()) {
						if (toyController.connectDevice()) {
							setState(getStateCounter(), 5);
							player.sendMessage(new LiteralText(String.format(
									"Connected to " + Formatting.GREEN + "%s" + Formatting.RESET + " [%d]",
									toyController.getDeviceName(), toyController.getDeviceId())), true);
						} else {
							player.sendMessage(new LiteralText(String.format(
									Formatting.GREEN + PlugnPlay.MOD_NAME + " " + Formatting.RESET + "failed to start\n%s",
									toyController.getLastErrorMessage())), false);
						}
					}
				}
			}
		}
	}

	public static void onXpChange(PlayerEntity player, int xpChange) {
		GameProfile profile = player.getGameProfile();

		if (profile.getId().equals(playerID)) {
			long duration = Math.round(Math.ceil(Math.log(xpChange + 0.5)));

			LOGGER.debug("XP CHANGE: " + xpChange);
			LOGGER.debug("duration: " + duration);

			setState(getStateCounter(), Math.toIntExact(duration), config.xpChangeIntensity, true);
		}
	}

	private static boolean isSprinting;

	public static void sprintingChanged(boolean sprinting) {
		isSprinting = sprinting && config.sprintingIntensity > 0;
		if (isSprinting) {
			setSprintVibrations();
		}
	}

	private static void setSprintVibrations() {
		setState(getStateCounter(), 1, config.sprintingIntensity, true);
	}

	public static void onPlace(ItemPlacementContext context, BlockState state) {
		if (context.getPlayer().getGameProfile().getId().equals(playerID)) {
			setState(getStateCounter(), 1, (int) (config.placeBlockIntensity * (state.getBlock().getHardness() / 70f + 0.29f)), true);
		}
	}
}
