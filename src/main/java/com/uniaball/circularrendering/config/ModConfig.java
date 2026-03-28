package com.uniaball.circularrendering.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("circular-rendering.json");
    private static ModConfig INSTANCE;

    public double renderRadiusScale = 1.0;
    public boolean enableVerticalRange = false;
    public int verticalRange = 16;
    public Preset preset = Preset.BALANCED;
    public boolean customMode = false;

    public enum Preset {
        AGGRESSIVE,
        PERFORMANCE,
        BALANCED
    }

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static ModConfig load() {
        ModConfig config;
        if (Files.exists(CONFIG_PATH)) {
            try (var reader = Files.newBufferedReader(CONFIG_PATH)) {
                config = GSON.fromJson(reader, ModConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
                config = new ModConfig();
            }
        } else {
            config = new ModConfig();
        }

        config.syncFieldsFromPreset();
        config.save();

        return config;
    }

    private void syncFieldsFromPreset() {
        if (!customMode) {
            applyPreset(preset);
        } else {
            Preset matched = getMatchingPreset();
            if (matched != null && matched != preset) {
                preset = matched;
            }
        }
    }

    private void applyPreset(Preset preset) {
        switch (preset) {
            case AGGRESSIVE:
                renderRadiusScale = 0.4;
                enableVerticalRange = true;
                verticalRange = 3;
                break;
            case PERFORMANCE:
                renderRadiusScale = 0.8;
                enableVerticalRange = true;
                verticalRange = 10;
                break;
            case BALANCED:
                renderRadiusScale = 1.0;
                enableVerticalRange = false;
                verticalRange = 16;
                break;
        }
    }

    private Preset getMatchingPreset() {
        if (Math.abs(renderRadiusScale - 1.0) < 1e-6 && !enableVerticalRange) {
            return Preset.BALANCED;
        } else if (Math.abs(renderRadiusScale - 0.4) < 1e-6 && enableVerticalRange && verticalRange == 3) {
            return Preset.AGGRESSIVE;
        } else if (Math.abs(renderRadiusScale - 0.8) < 1e-6 && enableVerticalRange && verticalRange == 10) {
            return Preset.PERFORMANCE;
        }
        return null;
    }

    public void save() {
        try (var writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}