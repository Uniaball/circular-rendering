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

        OptionGroupBuilder presetGroup = builder.createOptionGroup();
        presetGroup.setName(Component.translatable("circular-rendering.group.preset"));

        Identifier presetId = Identifier.parse("circular-rendering:preset");
        presetGroup.addOption(builder.createEnumOption(presetId, ModConfig.Preset.class)
                .setName(Component.translatable("circular-rendering.option.preset"))
                .setTooltip(Component.translatable("circular-rendering.option.preset.tooltip"))
                .setStorageHandler(config::save)
                .setBinding(
                        (ModConfig.Preset value) -> {
                            if (applyingPreset) return;
                            applyingPreset = true;
                            config.applyPreset(value);
                            applyingPreset = false;
                        },
                        () -> config.preset
                )
                .setDefaultValue(ModConfig.Preset.BALANCED)
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
                        case CUSTOM:
                            name = Component.translatable("circular-rendering.preset.custom")
                                    .copy().withStyle(ChatFormatting.AQUA);
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
                        (Integer value) -> config.renderRadiusScale = value / 100.0,
                        () -> (int) Math.round(config.renderRadiusScale * 100)
                )
                .setEnabledProvider(state -> state.readEnumOption(presetId, ModConfig.Preset.class) == ModConfig.Preset.CUSTOM, presetId)
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
                        (Boolean value) -> config.enableVerticalRange = value,
                        () -> config.enableVerticalRange
                )
                .setEnabledProvider(state -> state.readEnumOption(presetId, ModConfig.Preset.class) == ModConfig.Preset.CUSTOM, presetId)
                .setDefaultValue(false));

        verticalGroup.addOption(builder.createIntegerOption(Identifier.parse("circular-rendering:vertical_range"))
                .setName(Component.translatable("circular-rendering.option.vertical_range"))
                .setTooltip(Component.translatable("circular-rendering.option.vertical_range.tooltip"))
                .setRange(1, 32, 1)
                .setStorageHandler(config::save)
                .setBinding(
                        (Integer value) -> config.verticalRange = value,
                        () -> config.verticalRange
                )
                .setEnabledProvider(state ->
                        state.readEnumOption(presetId, ModConfig.Preset.class) == ModConfig.Preset.CUSTOM &&
                                state.readBooleanOption(enableId),
                        presetId, enableId)
                .setDefaultValue(16)
                .setValueFormatter(v -> Component.literal(v + " " + Component.translatable("circular-rendering.option.vertical_range.unit").getString()))
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));
        page.addOptionGroup(verticalGroup);

        builder.registerOwnModOptions()
                .setName("Circular Rendering")
                .setIcon(Identifier.parse("circular-rendering:icon.png"))
                .addPage(page);
    }
}