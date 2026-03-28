package com.uniaball.circularrendering.integration;

import com.uniaball.circularrendering.config.ModConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SodiumIntegration implements ConfigEntryPoint {
    private final ModConfig config = ModConfig.getInstance();
    private boolean applyingPreset = false;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OptionPageBuilder page = builder.createOptionPage();
        page.setName(Component.translatable("circular-rendering.options.title"));

        OptionGroupBuilder modeGroup = builder.createOptionGroup();
        modeGroup.setName(Component.translatable("circular-rendering.group.mode"));

        Identifier customModeId = Identifier.parse("circular-rendering:custom_mode");
        modeGroup.addOption(builder.createBooleanOption(customModeId)
                .setName(Component.translatable("circular-rendering.option.custom_mode"))
                .setTooltip(Component.translatable("circular-rendering.option.custom_mode.tooltip"))
                .setStorageHandler(config::save)
                .setBinding(
                        (Boolean value) -> {
                            boolean old = config.customMode;
                            config.customMode = value;
                            if (old && !value) {
                                applyMatchingPreset();
                            }
                            config.save();
                        },
                        () -> config.customMode
                )
                .setDefaultValue(false)
        );
        page.addOptionGroup(modeGroup);

        OptionGroupBuilder presetGroup = builder.createOptionGroup();
        presetGroup.setName(Component.translatable("circular-rendering.group.preset"));

        Identifier presetId = Identifier.parse("circular-rendering:preset");
        presetGroup.addOption(builder.createEnumOption(presetId, ModConfig.Preset.class)
                .setName(Component.translatable("circular-rendering.option.preset"))
                .setTooltip(Component.translatable("circular-rendering.option.preset.tooltip"))
                .setStorageHandler(config::save)
                .setBinding(
                        (ModConfig.Preset value) -> {
                            if (applyingPreset) {
                                config.preset = value;
                                config.save();
                                return;
                            }
                            applyingPreset = true;
                            config.preset = value;
                            applyPreset(value);
                            config.save();
                            applyingPreset = false;
                        },
                        () -> config.preset
                )
                .setDefaultValue(ModConfig.Preset.BALANCED)
                .setEnabledProvider(state -> !state.readBooleanOption(customModeId), customModeId)
                .setElementNameProvider(preset -> {
                    Component name;
                    switch (preset) {
                        case AGGRESSIVE:
                            name = Component.translatable("circular-rendering.preset.aggressive")
                                    .copy().withStyle(ChatFormatting.RED);
                            break;
                        case PERFORMANCE:
                            name = Component.translatable("circular-rendering.preset.performance")
                                    .copy().withStyle(ChatFormatting.GOLD);
                            break;
                        case BALANCED:
                            name = Component.translatable("circular-rendering.preset.balanced")
                                    .copy().withStyle(ChatFormatting.GREEN);
                            break;
                        default:
                            name = Component.literal(preset.name());
                    }
                    return name;
                })
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
        );
        page.addOptionGroup(presetGroup);

        OptionGroupBuilder circleGroup = builder.createOptionGroup();
        circleGroup.setName(Component.translatable("circular-rendering.group.circle"));
        circleGroup.addOption(builder.createIntegerOption(Identifier.parse("circular-rendering:render_radius_scale"))
                .setName(Component.translatable("circular-rendering.option.render_radius_scale"))
                .setTooltip(Component.translatable("circular-rendering.option.render_radius_scale.tooltip"))
                .setRange(10, 100, 1)
                .setStorageHandler(config::save)
                .setBinding(
                        (Integer value) -> {
                            double newScale = value / 100.0;
                            config.renderRadiusScale = newScale;
                            if (config.customMode) {
                                updatePresetIfNeeded();
                            }
                            config.save();
                        },
                        () -> (int) Math.round(config.renderRadiusScale * 100)
                )
                .setEnabledProvider(state -> state.readBooleanOption(customModeId), customModeId)
                .setDefaultValue(100)
                .setValueFormatter(v -> Component.literal(v + "%"))
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));
        page.addOptionGroup(circleGroup);

        Identifier enableId = Identifier.parse("circular-rendering:enable_vertical_range");
        OptionGroupBuilder verticalGroup = builder.createOptionGroup();
        verticalGroup.setName(Component.translatable("circular-rendering.group.vertical"));

        verticalGroup.addOption(builder.createBooleanOption(enableId)
                .setName(Component.translatable("circular-rendering.option.enable_vertical_range"))
                .setTooltip(Component.translatable("circular-rendering.option.enable_vertical_range.tooltip"))
                .setStorageHandler(config::save)
                .setBinding(
                        (Boolean value) -> {
                            config.enableVerticalRange = value;
                            if (config.customMode) {
                                updatePresetIfNeeded();
                            }
                            config.save();
                        },
                        () -> config.enableVerticalRange
                )
                .setEnabledProvider(state -> state.readBooleanOption(customModeId), customModeId)
                .setDefaultValue(false));

        verticalGroup.addOption(builder.createIntegerOption(Identifier.parse("circular-rendering:vertical_range"))
                .setName(Component.translatable("circular-rendering.option.vertical_range"))
                .setTooltip(Component.translatable("circular-rendering.option.vertical_range.tooltip"))
                .setRange(1, 32, 1)
                .setStorageHandler(config::save)
                .setBinding(
                        (Integer value) -> {
                            config.verticalRange = value;
                            if (config.customMode) {
                                updatePresetIfNeeded();
                            }
                            config.save();
                        },
                        () -> config.verticalRange
                )
                .setEnabledProvider(state -> state.readBooleanOption(enableId) && state.readBooleanOption(customModeId), enableId, customModeId)
                .setDefaultValue(16)
                .setValueFormatter(v -> Component.literal(v + " " + Component.translatable("circular-rendering.option.vertical_range.unit").getString()))
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));
        page.addOptionGroup(verticalGroup);

        builder.registerOwnModOptions()
                .setName("Circular Rendering")
                .setIcon(Identifier.parse("circular-rendering:icon.png"))
                .addPage(page);
    }

    private void applyPreset(ModConfig.Preset preset) {
        switch (preset) {
            case AGGRESSIVE:
                config.renderRadiusScale = 0.4;
                config.enableVerticalRange = true;
                config.verticalRange = 3;
                break;
            case PERFORMANCE:
                config.renderRadiusScale = 0.8;
                config.enableVerticalRange = true;
                config.verticalRange = 10;
                break;
            case BALANCED:
                config.renderRadiusScale = 1.0;
                config.enableVerticalRange = false;
                config.verticalRange = 16;
                break;
        }
    }

    private void applyMatchingPreset() {
        ModConfig.Preset matched = getMatchingPreset();
        if (matched != null) {
            applyingPreset = true;
            config.preset = matched;
            applyPreset(matched);
            applyingPreset = false;
        } else {
            applyingPreset = true;
            config.preset = ModConfig.Preset.BALANCED;
            applyPreset(ModConfig.Preset.BALANCED);
            applyingPreset = false;
        }
    }

    private ModConfig.Preset getMatchingPreset() {
        if (Math.abs(config.renderRadiusScale - 1.0) < 1e-6 && !config.enableVerticalRange) {
            return ModConfig.Preset.BALANCED;
        } else if (Math.abs(config.renderRadiusScale - 0.3) < 1e-6 && config.enableVerticalRange && config.verticalRange == 3) {
            return ModConfig.Preset.AGGRESSIVE;
        } else if (Math.abs(config.renderRadiusScale - 0.7) < 1e-6 && config.enableVerticalRange && config.verticalRange == 10) {
            return ModConfig.Preset.PERFORMANCE;
        }
        return null;
    }

    private void updatePresetIfNeeded() {
        if (!config.customMode) return;
        ModConfig.Preset matched = getMatchingPreset();
        if (matched != null && config.preset != matched) {
            config.preset = matched;
            config.save();
        }
    }
}