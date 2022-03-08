package io.github.watermel0nium.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import io.github.watermel0nium.PlugnPlay;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

import static io.github.watermel0nium.config.ConfigHandler.*;

public class PlugnPlayModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::makeConfig;
    }

    private Screen makeConfig(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new LiteralText(PlugnPlay.MOD_NAME + " Settings"))
                .setSavingRunnable(ConfigHandler::save);

        var configEntryBuilder = builder.entryBuilder();

        builder.getOrCreateCategory(new LiteralText("General"))
                .addEntry(configEntryBuilder.startStrField(new LiteralText("Server URL"), config.serverURL)
                        .setDefaultValue("ws://localhost:12345/buttplug")
                        .setSaveConsumer(s -> config.serverURL = s)
                        .build())
                .addEntry(configEntryBuilder.startBooleanToggle(new LiteralText("Vibrate"), config.vibrate)
                        .setDefaultValue(true)
                        .setSaveConsumer(v -> config.vibrate = v)
                        .build())
                .addEntry(configEntryBuilder
                        .startEnumSelector(new LiteralText("Gameplay Mode"), GameplayMode.class,
                                config.mode)
                        .setDefaultValue(GameplayMode.NORMAL)
                        .setSaveConsumer(mode -> {
                            config.mode = mode;
                            switch(mode) {
                                case NORMAL -> config.setIntensities(Intensities.NORMAL);
                                case MASOCHIST -> config.setIntensities(Intensities.MASOCHIST);
                                case HEDONIST -> config.setIntensities(Intensities.HEDONIST);
                            }
                        })
                        .build());

        builder.getOrCreateCategory(new LiteralText("Custom"))
                .addEntry(configEntryBuilder
                        .startIntSlider(new LiteralText("Attack Intensity"), config.attackIntensity, 0,
                                100)
                        .setDefaultValue(50)
                        .setSaveConsumer(ai -> config.attackIntensity = ai)
                        .setTooltip(new LiteralText("Set multiplier for vibrations for you attack a entity."))
                        .build())
                .addEntry(configEntryBuilder
                        .startIntSlider(new LiteralText("Hurt Intensity"), config.hurtIntensity, 0,
                                100)
                        .setDefaultValue(50)
                        .setSaveConsumer(hi -> config.hurtIntensity = hi)
                        .setTooltip(new LiteralText("Set multiplier for vibrations for taken damage.\nVibrates more based on damage. (max at 2 hearts) unless the damage source is an explosion."))
                        .build())
                .addEntry(configEntryBuilder
                        .startIntSlider(new LiteralText("Mine Intensity"), config.mineIntensity, 0,
                                100)
                        .setDefaultValue(0)
                        .setSaveConsumer(mi -> config.mineIntensity = mi)
                        .build())
                .addEntry(configEntryBuilder
                        .startIntSlider(new LiteralText("XP Changed Intensity"),
                                config.xpChangeIntensity, 0, 100)
                        .setDefaultValue(0)
                        .setSaveConsumer(xci -> config.xpChangeIntensity = xci)
                        .build())
                .addEntry(configEntryBuilder
                        .startIntSlider(new LiteralText("Harvest Intensity"), config.harvestIntensity,
                                0, 100)
                        .setDefaultValue(100)
                        .setSaveConsumer(hi -> config.harvestIntensity = hi)
                        .setTooltip(new LiteralText("Set multiplier for vibrations when breaking blocks. (Multiplied with block hardness)"))
                        .build())
                .addEntry(configEntryBuilder
                        .startIntSlider(new LiteralText("Vitality Intensity"),
                                config.vitalityIntensity, 0, 100)
                        .setDefaultValue(50)
                        .setSaveConsumer(vi -> config.vitalityIntensity = vi)
                        .setTooltip(new LiteralText("Set multiplier for vibrations for when heath is missing."))
                        .build())
                .addEntry(configEntryBuilder
                        .startIntSlider(new LiteralText("Sprinting Intensity"),
                                config.sprintingIntensity, 0, 100)
                        .setDefaultValue(0)
                        .setSaveConsumer(si -> config.sprintingIntensity = si)
                        .setTooltip(new LiteralText("Set amount of vibrations during spiriting."))
                        .build())
                .addEntry(configEntryBuilder
                        .startIntSlider(new LiteralText("Place Block Intensity"),
                                config.placeBlockIntensity, 0, 100)
                        .setDefaultValue(100)
                        .setSaveConsumer(pbi -> config.placeBlockIntensity = pbi)
                        .setTooltip(new LiteralText("Set multiplier for vibrations when placing blocks. (Multiplied with block hardness)"))
                        .build());
        return builder.build();
    }

}
