package com.uniaball.circularrendering;

import com.uniaball.circularrendering.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class CircularRenderingMod implements ClientModInitializer {
    public static final String MOD_ID = "circularrendering";
    public static final String MOD_NAME = "Circular Rendering";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    
    @Override
    public void onInitializeClient() {
        ModConfig.init();
        
        LOGGER.info("Circular Rendering Mod initialized successfully!");
    }
}
