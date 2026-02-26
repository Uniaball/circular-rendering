package com.uniaball.circularrendering;

import com.uniaball.circularrendering.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircularRenderingMod implements ModInitializer {
    public static final String MOD_ID = "circular-rendering";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.getInstance();
        LOGGER.info("Circular Rendering initialized!");
    }
}