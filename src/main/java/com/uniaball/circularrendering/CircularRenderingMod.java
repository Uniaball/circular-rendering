package com.uniaball.circularrendering;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircularRenderingMod implements ModInitializer {
    public static final String MOD_ID = "circular-rendering";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Circular Rendering mod initialized!");
    }
}