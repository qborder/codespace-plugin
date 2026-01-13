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

    public static void handleMessage(String playerName, String query) {
        AIChatbotClient.LOGGER.info("[AICHAT] Processing query from " + playerName + ": " + query);

        long now = System.currentTimeMillis();
        if (now - lastRequest < Config.cooldownMs) {
            AIChatbotClient.LOGGER.info("[AICHAT] Cooldown active");
            return;
        }
        lastRequest = now;

        if (query.isEmpty()) {
            sendChat("Usage: " + Config.prefix + " <your question>");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                AIChatbotClient.LOGGER.info("[AICHAT] Calling API...");
                String response = callAPI(playerName, query);
                AIChatbotClient.LOGGER.info("[AICHAT] Got response: " + response);
                if (response != null && !response.isEmpty()) {
                    sendChat(response);
                }
            } catch (Exception e) {
                AIChatbotClient.LOGGER.error("[AICHAT] API failed", e);
                sendChat("Error: " + e.getMessage());
            }
        });
    }

    private static String callAPI(String playerName, String query) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("provider", Config.provider);
        body.addProperty("model", Config.model);
        body.addProperty("stream", false);
        body.addProperty("temperature", 1);
        body.addProperty("top_p", 1);
        body.addProperty("max_tokens", Config.maxTokens);
        body.addProperty("presence_penalty", 0);
        body.addProperty("frequency_penalty", 0);

        JsonArray messages = new JsonArray();

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", Config.systemPrompt);
        messages.add(sys);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", playerName + " asks: " + query);
        messages.add(user);

        body.add("messages", messages);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(Config.apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Config.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

        AIChatbotClient.LOGGER.info("[AICHAT] API status: " + res.statusCode());

        if (res.statusCode() != 200) {
            AIChatbotClient.LOGGER.error("[AICHAT] API error: " + res.body());
            return "API error " + res.statusCode();
        }

        JsonObject json = GSON.fromJson(res.body(), JsonObject.class);
        return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString()
                .trim();
    }

    private static void sendChat(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.execute(() -> {
                String toSend = msg.length() > 256 ? msg.substring(0, 253) + "..." : msg;
                AIChatbotClient.LOGGER.info("[AICHAT] Sending: " + toSend);
                mc.player.networkHandler.sendChatMessage(toSend);
            });
        }
    }
}
