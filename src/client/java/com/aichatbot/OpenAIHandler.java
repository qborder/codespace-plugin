package com.aichatbot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.net.URI;
import java.net.http.*;
import java.util.concurrent.CompletableFuture;

public class OpenAIHandler {
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static long lastRequest = 0;

    public static void handleMessage(String playerName, String query) {
        if (!Config.enabled)
            return;

        AIChatbotClient.LOGGER.info("[AICHAT] Query from " + playerName + ": " + query);

        long now = System.currentTimeMillis();
        if (now - lastRequest < Config.cooldownMs) {
            return;
        }
        lastRequest = now;

        if (query.isEmpty()) {
            sendChat("Usage: " + Config.prefix + " <your question>");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String response = callAPI(playerName, query);
                if (response != null && !response.isEmpty()) {
                    String cleaned = sanitizeForMinecraft(response);
                    String finalMsg = Config.responsePrefix + " " + cleaned;
                    sendChat(finalMsg);
                }
            } catch (Exception e) {
                AIChatbotClient.LOGGER.error("[AICHAT] API failed", e);
            }
        });
    }

    private static String sanitizeForMinecraft(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 32 && c <= 126) {
                sb.append(c);
            } else if (c == '\n' || c == '\r') {
                sb.append(' ');
            }
        }

        String result = sb.toString()
                .replace("\"", "'")
                .replace("\\", "/")
                .replaceAll("\\s+", " ")
                .trim();

        return result;
    }

    private static String callAPI(String playerName, String query) throws Exception {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(Config.systemPrompt);

        if (player != null) {
            contextBuilder.append("\n\n--- MINECRAFT CONTEXT ---");

            if (Config.includePlayerInfo) {
                contextBuilder.append("\nYour username: ").append(player.getName().getString());
                contextBuilder.append("\nAsking player: ").append(playerName);
                contextBuilder.append("\nHealth: ").append((int) player.getHealth()).append("/20");
                contextBuilder.append("\nHunger: ").append(player.getHungerManager().getFoodLevel()).append("/20");
            }

            if (Config.includeCoords) {
                BlockPos pos = player.getBlockPos();
                contextBuilder.append("\nYour coordinates: X=").append(pos.getX())
                        .append(" Y=").append(pos.getY())
                        .append(" Z=").append(pos.getZ());
                contextBuilder.append("\nDimension: ")
                        .append(player.getEntityWorld().getRegistryKey().getValue().toString());
            }

            contextBuilder.append("\n--- END CONTEXT ---");
        }

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
        sys.addProperty("content", contextBuilder.toString());
        messages.add(sys);

        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", playerName + ": " + query);
        messages.add(user);

        body.add("messages", messages);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(Config.apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + Config.apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200) {
            AIChatbotClient.LOGGER.error("[AICHAT] API error: " + res.body());
            return null;
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
                mc.player.networkHandler.sendChatMessage(toSend);
            });
        }
    }
}
