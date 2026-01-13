package com.aichatbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.*;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "aichatbot.json");

    public static String apiUrl = "https://ai-chat-api-lake.vercel.app/api/chat";
    public static String apiKey = "sk-aichat-default-key-2024";
    public static String provider = "chat.gpt-chatbot.ru";
    public static String model = "chatgpt-4o-latest";
    public static String prefix = "!ai";
    public static String systemPrompt = "You are a helpful Minecraft assistant. Keep responses short (under 200 characters) since this is in-game chat. Be friendly and concise.";
    public static int cooldownMs = 2000;
    public static int maxTokens = 150;

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                JsonObject obj = GSON.fromJson(json, JsonObject.class);

                if (obj.has("apiUrl"))
                    apiUrl = obj.get("apiUrl").getAsString();
                if (obj.has("apiKey"))
                    apiKey = obj.get("apiKey").getAsString();
                if (obj.has("provider"))
                    provider = obj.get("provider").getAsString();
                if (obj.has("model"))
                    model = obj.get("model").getAsString();
                if (obj.has("prefix"))
                    prefix = obj.get("prefix").getAsString();
                if (obj.has("systemPrompt"))
                    systemPrompt = obj.get("systemPrompt").getAsString();
                if (obj.has("cooldownMs"))
                    cooldownMs = obj.get("cooldownMs").getAsInt();
                if (obj.has("maxTokens"))
                    maxTokens = obj.get("maxTokens").getAsInt();
            } else {
                save();
            }
        } catch (Exception e) {
            AIChatbotClient.LOGGER.error("Failed to load config", e);
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            JsonObject obj = new JsonObject();
            obj.addProperty("apiUrl", apiUrl);
            obj.addProperty("apiKey", apiKey);
            obj.addProperty("provider", provider);
            obj.addProperty("model", model);
            obj.addProperty("prefix", prefix);
            obj.addProperty("systemPrompt", systemPrompt);
            obj.addProperty("cooldownMs", cooldownMs);
            obj.addProperty("maxTokens", maxTokens);

            Files.writeString(CONFIG_PATH, GSON.toJson(obj));
        } catch (Exception e) {
            AIChatbotClient.LOGGER.error("Failed to save config", e);
        }
    }
}
