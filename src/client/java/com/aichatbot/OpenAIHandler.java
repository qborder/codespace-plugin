package com.aichatbot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;

public class OpenAIHandler {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static long lastRequest = 0;

    public static void handleMessage(String playerName, String message) {
        if (!message.toLowerCase().startsWith(Config.prefix.toLowerCase())) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastRequest < Config.cooldownMs) {
            return;
        }
        lastRequest = now;

        String query = message.substring(Config.prefix.length()).trim();
        if (query.isEmpty()) {
            sendChat("Usage: " + Config.prefix + " <your question>");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String response = callAPI(playerName, query);
                if (response != null && !response.isEmpty()) {
                    sendChat(response);
                }
            } catch (Exception e) {
                AIChatbotClient.LOGGER.error("API request failed", e);
                sendChat("Error: AI request failed");
            }
        });
    }

    private static String callAPI(String playerName, String query) throws Exception {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("provider", Config.provider);
        requestBody.addProperty("model", Config.model);
        requestBody.addProperty("stream", false);
        requestBody.addProperty("temperature", 1);
        requestBody.addProperty("top_p", 1);
        requestBody.addProperty("max_tokens", Config.maxTokens);
        requestBody.addProperty("presence_penalty", 0);
        requestBody.addProperty("frequency_penalty", 0);

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", Config.systemPrompt);
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", playerName + " asks: " + query);
        messages.add(userMsg);

        requestBody.add("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Config.apiKey)
                .header("Accept", "*/*")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(requestBody)))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            AIChatbotClient.LOGGER.error("API error: " + response.body());
            return "Error: API returned " + response.statusCode();
        }

        JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
        return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString()
                .trim();
    }

    private static void sendChat(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.player.networkHandler != null) {
            client.execute(() -> {
                String toSend = message.length() > 256 ? message.substring(0, 253) + "..." : message;
                client.player.networkHandler.sendChatMessage(toSend);
            });
        }
    }
}
