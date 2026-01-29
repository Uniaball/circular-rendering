package com.uniaball.circularrendering.config;

public class ModConfig {
    public static void init() {
        CircularRenderingConfig.loadConfig();
    }
    
    public static void save() {
        CircularRenderingConfig.saveConfig();
    }
    
    public static void resetCache() {
        com.uniaball.circularrendering.render.ChunkCuller.resetCache();
    }
}
