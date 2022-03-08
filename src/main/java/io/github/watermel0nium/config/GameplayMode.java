package io.github.watermel0nium.config;

import java.util.Objects;

import io.github.watermel0nium.PlugnPlay;

public enum GameplayMode {
	NORMAL("gui." + PlugnPlay.MOD_ID + ".config.mode.normal"),
	MASOCHIST("gui." + PlugnPlay.MOD_ID + ".config.mode.masochist"),
	HEDONIST("gui." + PlugnPlay.MOD_ID + ".config.mode.hedonist"),
	CUSTOM("gui." + PlugnPlay.MOD_ID + ".config.mode.custom");

	private final String translateKey;

	GameplayMode(String translateKey) {
		this.translateKey = Objects.requireNonNull(translateKey, "translateKey");
	}

	public String getTranslateKey() {
		return this.translateKey;
	}
}
