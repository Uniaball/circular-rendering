package com.uniaball.circularrendering.integration;

import com.uniaball.circularrendering.config.CircularRenderingConfig;
import com.uniaball.circularrendering.config.ModConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.structure.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SodiumIntegration implements ConfigEntryPoint {
    
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        CircularRenderingConfig config = CircularRenderingConfig.getInstance();
        
        OptionPageBuilder page = builder.createOptionPage();
        page.setName(Text.translatable("circularrendering.options.title"));
        
        OptionGroupBuilder mainGroup = builder.createOptionGroup();
        mainGroup.setName(Text.translatable("circularrendering.group.main"));
        
        mainGroup.addOption(builder.createBooleanOption(Identifier.of("circularrendering", "enabled"))
                .setName(Text.translatable("circularrendering.option.enabled"))
                .setTooltip(Text.translatable("circularrendering.option.enabled.tooltip"))
                .setStorageHandler(ModConfig::save)
                .setBinding(v -> { 
                    config.enabled = v; 
                    ModConfig.resetCache(); 
                }, () -> config.enabled)
                .setDefaultValue(true)
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));
        
        mainGroup.addOption(builder.createBooleanOption(Identifier.of("circularrendering", "use_3d_circle"))
                .setName(Text.translatable("circularrendering.option.use_3d_circle"))
                .setTooltip(Text.translatable("circularrendering.option.use_3d_circle.tooltip"))
                .setStorageHandler(ModConfig::save)
                .setBinding(v -> { 
                    config.use3DCircle = v; 
                    ModConfig.resetCache(); 
                }, () -> config.use3DCircle)
                .setDefaultValue(true)
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));
        
        mainGroup.addOption(builder.createIntegerOption(Identifier.of("circularrendering", "radius_multiplier"))
                .setName(Text.translatable("circularrendering.option.radius_multiplier"))
                .setTooltip(Text.translatable("circularrendering.option.radius_multiplier.tooltip"))
                .setRange(5, 20, 1)
                .setStorageHandler(ModConfig::save)
                .setValueFormatter(v -> Text.literal(String.format("%.1fx", v / 10.0)))
                .setBinding(v -> { 
                    config.radiusMultiplier = v / 10.0; 
                    ModConfig.resetCache(); 
                }, () -> (int)(config.radiusMultiplier * 10))
                .setDefaultValue(10)
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD));
        
        page.addOptionGroup(mainGroup);
        
        OptionGroupBuilder advancedGroup = builder.createOptionGroup();
        advancedGroup.setName(Text.translatable("circularrendering.group.advanced"));
        
        advancedGroup.addOption(builder.createBooleanOption(Identifier.of("circularrendering", "vertical_culling"))
                .setName(Text.translatable("circularrendering.option.vertical_culling"))
                .setTooltip(Text.translatable("circularrendering.option.vertical_culling.tooltip"))
                .setStorageHandler(ModConfig::save)
                .setBinding(v -> { 
                    config.verticalCulling = v; 
                    ModConfig.resetCache(); 
                }, () -> config.verticalCulling)
                .setDefaultValue(true));
        
        advancedGroup.addOption(builder.createIntegerOption(Identifier.of("circularrendering", "vertical_radius"))
                .setName(Text.translatable("circularrendering.option.vertical_radius"))
                .setTooltip(Text.translatable("circularrendering.option.vertical_radius.tooltip"))
                .setRange(8, 32, 1)
                .setStorageHandler(ModConfig::save)
                .setValueFormatter(v -> Text.literal(v + " chunks"))
                .setBinding(v -> { 
                    config.verticalRadius = v; 
                    ModConfig.resetCache(); 
                }, () -> config.verticalRadius)
                .setDefaultValue(16));
        
        advancedGroup.addOption(builder.createBooleanOption(Identifier.of("circularrendering", "debug_mode"))
                .setName(Text.translatable("circularrendering.option.debug_mode"))
                .setTooltip(Text.translatable("circularrendering.option.debug_mode.tooltip"))
                .setStorageHandler(ModConfig::save)
                .setBinding(v -> { 
                    config.debugMode = v; 
                    ModConfig.resetCache(); 
                }, () -> config.debugMode)
                .setDefaultValue(false));
        
        page.addOptionGroup(advancedGroup);
        
        builder.registerOwnModOptions()
                .setName("Circular Rendering")
                .addPage(page);
    }
}
