package com.therainbowville.minegasm.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import org.apache.logging.log4j.LogManager;

import com.mojang.authlib.GameProfile;
import com.therainbowville.minegasm.config.GameplayMode;
import com.therainbowville.minegasm.config.MinegasmConfig;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ClientEventHandler {
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
	private static String playerName = null;
	private static UUID playerID = null;
	private static final int TICKS_PER_SECOND = 20;
	private static int tickCounter = -1;
	private static int clientTickCounter = -1;
	private static final double[] state = new double[1200];
	private static boolean paused = false;

	private static void clearState() {
		playerName = null;
		playerID = null;
		tickCounter = -1;
		clientTickCounter = -1;
		Arrays.fill(state, 0);
		paused = false;
	}

	private static Map<String, Integer> masochist = new HashMap<>();
	private static Map<String, Integer> hedonist = new HashMap<>();
	private static Map<String, Integer> normal = new HashMap<>();
	private static Map<String, Integer> custom = new HashMap<>();

	public static void BuildHashMaps() {
		masochist.put("attack", 0);
		masochist.put("hurt", 100);
		masochist.put("mine", 0);
		masochist.put("xpChange", 0);
		masochist.put("harvest", 100);
		masochist.put("vitality", 100);
		masochist.put("sprinting", 20);
		masochist.put("place", 0);

		hedonist.put("attack", 70);
		hedonist.put("hurt", 10);
		hedonist.put("mine", 00);
		hedonist.put("xpChange", 100);
		hedonist.put("harvest", 00);
		hedonist.put("vitality", 0);
		hedonist.put("sprinting", 0);
		hedonist.put("place", 0);

		normal.put("attack", 40);
		normal.put("hurt", 50);
		normal.put("mine", 0);
		normal.put("xpChange", 0);
		normal.put("harvest", 100);
		normal.put("vitality", 50);
		normal.put("sprinting", 0);
		hedonist.put("place", 100);

		reloadCustom();
	}

	public static void reloadCustom() {
		custom = new HashMap<>();
		custom.put("attack", MinegasmConfig.INSTANCE.attackIntensity);
		custom.put("hurt", MinegasmConfig.INSTANCE.hurtIntensity);
		custom.put("mine", MinegasmConfig.INSTANCE.mineIntensity);
		custom.put("xpChange", MinegasmConfig.INSTANCE.xpChangeIntensity);
		custom.put("harvest", MinegasmConfig.INSTANCE.harvestIntensity);
		custom.put("vitality", MinegasmConfig.INSTANCE.vitalityIntensity);
		custom.put("sprinting", MinegasmConfig.INSTANCE.sprintIntensity);
		custom.put("place", MinegasmConfig.INSTANCE.placeIntensity);
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
		int safeCounter = counter % state.length;
		if (accumulate) {
			state[safeCounter] = Math.min(1.0, state[safeCounter] + (intensity / 100.0));
		} else {
			state[safeCounter] = Math.min(1.0, Math.max(state[safeCounter], (intensity / 100.0)));
		}
	}

	private static int getIntensity(String type) {
		switch (MinegasmConfig.INSTANCE.mode) {
			case CUSTOM:
				return custom.get(type);
			case HEDONIST:
				return hedonist.get(type);
			case MASOCHIST:
				return masochist.get(type);
		}
		return normal.get(type);
	}

	public static ActionResult onAttack(PlayerEntity player, World world, Hand hand, Entity entity,
										EntityHitResult hitResult) {
		GameProfile profile = player.getGameProfile();

		if (profile.getId().equals(playerID)) {
			setState(getStateCounter(), 3, getIntensity("attack"), true);
		}
		return ActionResult.PASS;
	}

	private static int vitality = 0;

	public static void onHurt(GameProfile profile, DamageSource source, float amount) {
		if (profile.getId().equals(playerID)) {

			vitality -= amount;
			int vitalityIntensity = 0;
			if (vitality < 20 && vitality > 0) {
				vitalityIntensity = getIntensity("vitality");
				vitalityIntensity = (int) ((vitalityIntensity / 100f) * vitalityIntensity * (1 - vitality / 20f));
			}

			//set intensity strength depending on damage.
			int damageIntensity = getIntensity("hurt");
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
					int intensity = getIntensity("vitality");
					if (intensity > 0) {
						setState(stateCounter, (int) ((intensity / 100f) * intensity * (1 - vitality / 20f)));
					}
				}

				double newVibrationLevel = state[stateCounter];
				state[stateCounter] = 0;

				LOGGER.trace("Tick " + stateCounter + ": " + newVibrationLevel);

				ToyController.setVibrationLevel(newVibrationLevel);
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
					ToyController.setVibrationLevel(0);
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
		BuildHashMaps();
	}

	public static void onWorldLoaded(World world) {
		LOGGER.info("World loaded: " + world.toString());

		populatePlayerInfo();
	}

	public static void onDeath(PlayerEntity player) {
		GameProfile profile = player.getGameProfile();

		if (profile.getId().equals(playerID)) {
			ToyController.setVibrationLevel(0);
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
					= Math.toIntExact(Math.round((getIntensity("harvest") * (blockHardness / 50f))));

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
						= Math.toIntExact(Math.round((getIntensity("mine") / 100.0 * (blockHardness / 50.0)) * 100));
				setState(getStateCounter(), duration, intensity, true);
			}
		}
	}

	public static void onRespawn() {
		clearState();
		ToyController.setVibrationLevel(0);
		populatePlayerInfo();
	}

	public static void onWorldExit(Entity entity) {
		if ((entity instanceof PlayerEntity) && (playerName != null)) {
			clearState();
		}
	}

	public static void onWorldEntry(Entity entity) {
		if (entity instanceof ClientPlayerEntity) {
			LOGGER.info("Entered world: " + entity.toString());

			if (playerName != null) {
				PlayerEntity player = (PlayerEntity) entity;
				GameProfile profile = player.getGameProfile();

				if (profile.getId().equals(playerID)) {
					LOGGER.info("Player in: " + playerName + " " + playerID.toString());
					if (!ToyController.isConnected) {
						if (ToyController.connectDevice()) {
							setState(getStateCounter(), 5);
							player.sendMessage(new LiteralText(String.format(
									"Connected to " + Formatting.GREEN + "%s" + Formatting.RESET + " [%d]",
									ToyController.getDeviceName(), ToyController.getDeviceId())), true);
						} else {
							player.sendMessage(new LiteralText(String.format(
									Formatting.YELLOW + "Minegasm " + Formatting.RESET + "failed to start\n%s",
									ToyController.getLastErrorMessage())), false);
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

			setState(getStateCounter(), Math.toIntExact(duration), getIntensity("xpChange"), true);
		}
	}

	private static boolean isSprinting;

	public static void sprintingChanged(boolean sprinting) {
		isSprinting = sprinting && getIntensity("sprinting") > 0;
		if (isSprinting) {
			setSprintVibrations();
		}
	}

	private static void setSprintVibrations() {
		setState(getStateCounter(), 1, getIntensity("sprinting"), true);
	}

	public static void onPlace(ItemPlacementContext context, BlockState state) {
		if (context.getPlayer().getGameProfile().getId().equals(playerID)) {
			setState(getStateCounter(), 1, (int) (getIntensity("place") * (state.getBlock().getHardness() / 70f + 0.29f)), true);
		}
	}
}