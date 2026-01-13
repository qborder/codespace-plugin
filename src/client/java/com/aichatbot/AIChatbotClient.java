package com.aichatbot;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AIChatbotClient implements ClientModInitializer {
    public static final String MOD_ID = "aichatbot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        Config.load();
        LOGGER.info("AI Chatbot loaded! Prefix: " + Config.prefix);
    }
}
