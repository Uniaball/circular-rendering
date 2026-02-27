package com.uniaball.circularrendering.integration;

import com.uniaball.circularrendering.config.ModConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.BooleanProvider;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SodiumIntegration implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        ModConfig config = ModConfig.getInstance();

        OptionPageBuilder page = builder.createOptionPage();
        page.setName(Text.translatable("circular-rendering.options.title"));

        OptionGroupBuilder circleGroup = builder.createOptionGroup();
        circleGroup.setName(Text.translatable("circular-rendering.group.circle"));

        circleGroup.addOption(builder.createIntegerOption(Identifier.of("circular-rendering", "render_radius_scale"))
                .setName(Text.translatable("circular-rendering.option.render_radius_scale"))
                .setTooltip(Text.translatable("circular-rendering.option.render_radius_scale.tooltip"))
                .setRange(10, 100, 1)
                .setStorageHandler(config::save)
                .setBinding(
                    v -> {
                        config.renderRadiusScale = v / 100.0;
                        config.save();
                    },
                    () -> (int) Math.round(config.renderRadiusScale * 100)
                )
                .setDefaultValue(100)
                .setValueFormatter(v -> Text.literal(v + "%"))
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));

        page.addOptionGroup(circleGroup);

        OptionGroupBuilder verticalGroup = builder.createOptionGroup();
        verticalGroup.setName(Text.translatable("circular-rendering.group.vertical"));

        verticalGroup.addOption(builder.createBooleanOption(Identifier.of("circular-rendering", "enable_vertical_range"))
                .setName(Text.translatable("circular-rendering.option.enable_vertical_range"))
                .setTooltip(Text.translatable("circular-rendering.option.enable_vertical_range.tooltip"))
                .setStorageHandler(config::save)
                .setBinding(
                    v -> {
                        config.enableVerticalRange = v;
                        config.save();
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
                    v -> {
                        config.verticalRange = v;
                        config.save();
                    },
                    () -> config.verticalRange
                )
                .setDefaultValue(16)
                .setValueFormatter(v -> Text.literal(v + " " + Text.translatable("circular-rendering.option.vertical_range.unit").getString()))
                .setEnabledProvider((BooleanProvider) () -> config.enableVerticalRange, Identifier.of("circular-rendering", "enable_vertical_range"))
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));

        page.addOptionGroup(verticalGroup);

        builder.registerOwnModOptions()
                .setName("Circular Rendering")
                .addPage(page);
    }
}