package com.therainbowville.minegasm.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class MinegasmModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::makeConfig;
    }

    private Screen makeConfig(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new LiteralText("Minegasm Settings"))
                .setSavingRunnable(MinegasmConfig::saveConfig);

        ConfigEntryBuilder entryBuild = builder.entryBuilder();

        builder.getOrCreateCategory(new LiteralText("General"))
                .addEntry(entryBuild.startStrField(new LiteralText("Server URL"), MinegasmConfig.INSTANCE.serverUrl)
                        .setDefaultValue("ws://localhost:12345/buttplug")
                        .setSaveConsumer(s -> MinegasmConfig.INSTANCE.serverUrl = s)
                        .build())
                .addEntry(entryBuild.startBooleanToggle(new LiteralText("Vibrate"), MinegasmConfig.INSTANCE.vibrate)
                        .setDefaultValue(true)
                        .setSaveConsumer(v -> MinegasmConfig.INSTANCE.vibrate = v)
                        .build())
                .addEntry(entryBuild
                        .startEnumSelector(new LiteralText("Gameplay Mode"), GameplayMode.class,
                                MinegasmConfig.INSTANCE.mode)
                        .setDefaultValue(GameplayMode.NORMAL)
                        .setSaveConsumer(m -> {
                            MinegasmConfig.INSTANCE.mode = m;
                        })
                        .build());

        //if (MinegasmConfig.INSTANCE.mode == GameplayMode.CUSTOM) {
        builder.getOrCreateCategory(new LiteralText("Custom"))
                .addEntry(entryBuild
                        .startIntSlider(new LiteralText("Attack Intensity"), MinegasmConfig.INSTANCE.attackIntensity, 0,
                                100)
                        .setDefaultValue(50)
                        .setSaveConsumer(i -> MinegasmConfig.INSTANCE.attackIntensity = i)
                        .setTooltip(new LiteralText("Set multiplier for vibrations for you attack a entity."))
                        .build())
                .addEntry(entryBuild
                        .startIntSlider(new LiteralText("Hurt Intensity"), MinegasmConfig.INSTANCE.hurtIntensity, 0,
                                100)
                        .setDefaultValue(50)
                        .setSaveConsumer(i -> MinegasmConfig.INSTANCE.hurtIntensity = i)
                        .setTooltip(new LiteralText("Set multiplier for vibrations for taken damage.\nVibrates more based on damage. (max at 2 hearts) unless the damage source is an explosion."))
                        .build())
                .addEntry(entryBuild
                        .startIntSlider(new LiteralText("Mine Intensity"), MinegasmConfig.INSTANCE.mineIntensity, 0,
                                100)
                        .setDefaultValue(0)
                        .setSaveConsumer(i -> MinegasmConfig.INSTANCE.mineIntensity = i)
                        .build())
                .addEntry(entryBuild
                        .startIntSlider(new LiteralText("XP Changed Intensity"),
                                MinegasmConfig.INSTANCE.xpChangeIntensity, 0, 100)
                        .setDefaultValue(0)
                        .setSaveConsumer(i -> MinegasmConfig.INSTANCE.xpChangeIntensity = i)
                        .build())
                .addEntry(entryBuild
                        .startIntSlider(new LiteralText("Harvest Intensity"), MinegasmConfig.INSTANCE.harvestIntensity,
                                0, 100)
                        .setDefaultValue(100)
                        .setSaveConsumer(i -> MinegasmConfig.INSTANCE.harvestIntensity = i)
                        .setTooltip(new LiteralText("Set multiplier for vibrations when breaking blocks. (Multiplied with block hardness)"))
                        .build())
                .addEntry(entryBuild
                        .startIntSlider(new LiteralText("Vitality Intensity"),
                                MinegasmConfig.INSTANCE.vitalityIntensity, 0, 100)
                        .setDefaultValue(50)
                        .setSaveConsumer(i -> MinegasmConfig.INSTANCE.vitalityIntensity = i)
                        .setTooltip(new LiteralText("Set multiplier for vibrations for when heath is missing."))
                        .build())
                .addEntry(entryBuild
                        .startIntSlider(new LiteralText("Sprinting Intensity"),
                                MinegasmConfig.INSTANCE.sprintIntensity, 0, 100)
                        .setDefaultValue(0)
                        .setSaveConsumer(i -> MinegasmConfig.INSTANCE.sprintIntensity = i)
                        .setTooltip(new LiteralText("Set amount of vibrations during spiriting."))
                        .build())
                .addEntry(entryBuild
                        .startIntSlider(new LiteralText("Sprinting Intensity"),
                                MinegasmConfig.INSTANCE.placeIntensity, 0, 100)
                        .setDefaultValue(100)
                        .setSaveConsumer(i -> MinegasmConfig.INSTANCE.placeIntensity = i)
                        .setTooltip(new LiteralText("Set multiplier for vibrations when placing blocks. (Multiplied with block hardness)"))
                        .build());
        //}

        return builder.build();
    }

}