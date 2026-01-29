package com.uniaball.circularrendering.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CircularRenderingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static CircularRenderingConfig instance;
    
    public boolean enabled = true;
    public boolean use3DCircle = true;
    public double radiusMultiplier = 1.0;
    public boolean verticalCulling = true;
    public int verticalRadius = 16;
    public boolean debugMode = false;
    
    public static CircularRenderingConfig getInstance() {
        if (instance == null) {
            loadConfig();
        }
        return instance;
    }
    
    public static void loadConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("circularrendering.json");
        
        if (Files.exists(configPath)) {
            try (FileReader reader = new FileReader(configPath.toFile())) {
                instance = GSON.fromJson(reader, CircularRenderingConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
                instance = new CircularRenderingConfig();
            }
        } else {
            instance = new CircularRenderingConfig();
            saveConfig();
        }
    }
    
    public static void saveConfig() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("circularrendering.json");
        
        try (FileWriter writer = new FileWriter(configPath.toFile())) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
