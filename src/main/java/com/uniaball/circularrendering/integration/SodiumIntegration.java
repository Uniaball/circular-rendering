package com.uniaball.circularrendering.integration;

import com.uniaball.circularrendering.config.ModConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SodiumIntegration implements ConfigEntryPoint {
    private final ModConfig config = ModConfig.getInstance();
    private boolean applyingPreset = false;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        OptionPageBuilder page = builder.createOptionPage();
        page.setName(Text.translatable("circular-rendering.options.title"));

        OptionGroupBuilder presetGroup = builder.createOptionGroup();
        presetGroup.setName(Text.translatable("circular-rendering.group.preset"));

        Identifier presetId = Identifier.of("circular-rendering", "preset");
        presetGroup.addOption(builder.createEnumOption(presetId, ModConfig.Preset.class)
                .setName(Text.translatable("circular-rendering.option.preset"))
                .setTooltip(Text.translatable("circular-rendering.option.preset.tooltip"))
                .setStorageHandler(config::save)
                .setBinding(
                        (value, flush) -> {
                            if (applyingPreset) {
                                config.preset = value;
                                flush.accept(null);
                                return;
                            }
                            applyingPreset = true;
                            config.preset = value;
                            switch (value) {
                                case AGGRESSIVE:
                                    config.renderRadiusScale = 0.3;
                                    config.enableVerticalRange = true;
                                    config.verticalRange = 3;
                                    break;
                                case PERFORMANCE:
                                    config.renderRadiusScale = 0.7;
                                    config.enableVerticalRange = true;
                                    config.verticalRange = 10;
                                    break;
                                case BALANCED:
                                    config.renderRadiusScale = 1.0;
                                    config.enableVerticalRange = false;
                                    config.verticalRange = 16;
                                    break;
                                case CUSTOM:
                                    break;
                            }
                            config.save();
                            flush.accept(null);
                            applyingPreset = false;
                        },
                        () -> config.preset
                )
                .setDefaultValue(ModConfig.Preset.BALANCED)
                .setElementNameProvider(preset -> {
                    Text name;
                    switch (preset) {
                        case AGGRESSIVE:
                            name = Text.translatable("circular-rendering.preset.aggressive")
                                    .copy().formatted(Formatting.RED);
                            break;
                        case PERFORMANCE:
                            name = Text.translatable("circular-rendering.preset.performance")
                                    .copy().formatted(Formatting.GOLD);
                            break;
                        case BALANCED:
                            name = Text.translatable("circular-rendering.preset.balanced")
                                    .copy().formatted(Formatting.GREEN);
                            break;
                        case CUSTOM:
                            name = Text.translatable("circular-rendering.preset.custom")
                                    .copy().formatted(Formatting.GRAY);
                            break;
                        default:
                            name = Text.literal(preset.name());
                    }
                    return name;
                })
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
        );
        page.addOptionGroup(presetGroup);

        OptionGroupBuilder circleGroup = builder.createOptionGroup();
        circleGroup.setName(Text.translatable("circular-rendering.group.circle"));
        circleGroup.addOption(builder.createIntegerOption(Identifier.of("circular-rendering", "render_radius_scale"))
                .setName(Text.translatable("circular-rendering.option.render_radius_scale"))
                .setTooltip(Text.translatable("circular-rendering.option.render_radius_scale.tooltip"))
                .setRange(10, 100, 1)
                .setStorageHandler(config::save)
                .setBinding(
                        (value, flush) -> {
                            double newScale = value / 100.0;
                            if (applyingPreset) {
                                config.renderRadiusScale = newScale;
                                flush.accept(null);
                                return;
                            }
                            config.renderRadiusScale = newScale;
                            updatePresetIfNeeded();
                            flush.accept(null);
                        },
                        () -> (int) Math.round(config.renderRadiusScale * 100)
                )
                .setDefaultValue(100)
                .setValueFormatter(v -> Text.literal(v + "%"))
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));
        page.addOptionGroup(circleGroup);

        Identifier enableId = Identifier.of("circular-rendering", "enable_vertical_range");
        OptionGroupBuilder verticalGroup = builder.createOptionGroup();
        verticalGroup.setName(Text.translatable("circular-rendering.group.vertical"));

        verticalGroup.addOption(builder.createBooleanOption(enableId)
                .setName(Text.translatable("circular-rendering.option.enable_vertical_range"))
                .setTooltip(Text.translatable("circular-rendering.option.enable_vertical_range.tooltip"))
                .setStorageHandler(config::save)
                .setBinding(
                        (value, flush) -> {
                            if (applyingPreset) {
                                config.enableVerticalRange = value;
                                flush.accept(null);
                                return;
                            }
                            config.enableVerticalRange = value;
                            updatePresetIfNeeded();
                            flush.accept(null);
                        },
                        () -> config.enableVerticalRange
                )
                .setDefaultValue(false));

        verticalGroup.addOption(builder.createIntegerOption(Identifier.of("circular-rendering", "vertical_range"))
                .setName(Text.translatable("circular-rendering.option.vertical_range"))
                .setTooltip(Text.translatable("circular-rendering.option.vertical_range.tooltip"))
                .setRange(1, 32, 1)
                .setStorageHandler(config::save)
                .setBinding(
                        (value, flush) -> {
                            if (applyingPreset) {
                                config.verticalRange = value;
                                flush.accept(null);
                                return;
                            }
                            config.verticalRange = value;
                            updatePresetIfNeeded();
                            flush.accept(null);
                        },
                        () -> config.verticalRange
                )
                .setDefaultValue(16)
                .setValueFormatter(v -> Text.literal(v + " " + Text.translatable("circular-rendering.option.vertical_range.unit").getString()))
                .setEnabledProvider(state -> state.readBooleanOption(enableId), enableId)
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));
        page.addOptionGroup(verticalGroup);

        builder.registerOwnModOptions()
                .setName("Circular Rendering")
                .setIcon(Identifier.of("circular-rendering", "icon.png"))
                .addPage(page);
    }

    private void updatePresetIfNeeded() {
        if (applyingPreset) return;

        ModConfig.Preset newPreset = null;

        if (Math.abs(config.renderRadiusScale - 1.0) < 1e-6 && !config.enableVerticalRange) {
            newPreset = ModConfig.Preset.BALANCED;
        } else if (Math.abs(config.renderRadiusScale - 0.3) < 1e-6 && config.enableVerticalRange && config.verticalRange == 3) {
            newPreset = ModConfig.Preset.AGGRESSIVE;
        } else if (Math.abs(config.renderRadiusScale - 0.7) < 1e-6 && config.enableVerticalRange && config.verticalRange == 10) {
            newPreset = ModConfig.Preset.PERFORMANCE;
        } else {
            newPreset = ModConfig.Preset.CUSTOM;
        }

        if (config.preset != newPreset) {
            applyingPreset = true;
            config.preset = newPreset;
            config.save();
            applyingPreset = false;
        }
    }
}