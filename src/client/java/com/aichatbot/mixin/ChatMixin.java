package com.aichatbot.mixin;

import com.aichatbot.AIChatbotClient;
import com.aichatbot.Config;
import com.aichatbot.OpenAIHandler;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onChatMessage(Text message, CallbackInfo ci) {
        try {
            String raw = message.getString();
            AIChatbotClient.LOGGER.info("[AICHAT] Raw message: " + raw);

            String lowerRaw = raw.toLowerCase();
            String lowerPrefix = Config.prefix.toLowerCase();

            int prefixIndex = lowerRaw.indexOf(lowerPrefix);
            if (prefixIndex == -1) {
                return;
            }

            String playerName = extractPlayerName(raw, prefixIndex);
            String query = raw.substring(prefixIndex + Config.prefix.length()).trim();

            AIChatbotClient.LOGGER.info("[AICHAT] Detected prefix! Player: " + playerName + " | Query: " + query);

            if (!query.isEmpty()) {
                OpenAIHandler.handleMessage(playerName, query);
            }
        } catch (Exception e) {
            AIChatbotClient.LOGGER.error("[AICHAT] Error", e);
        }
    }

    private String extractPlayerName(String raw, int prefixIndex) {
        String beforePrefix = raw.substring(0, prefixIndex);

        int start = -1;
        int end = -1;
        for (int i = beforePrefix.length() - 1; i >= 0; i--) {
            char c = beforePrefix.charAt(i);
            if (c == '>' && end == -1) {
                end = i;
            } else if (c == '<' && end != -1) {
                start = i + 1;
                break;
            }
        }

        if (start != -1 && end != -1 && start < end) {
            return beforePrefix.substring(start, end);
        }

        for (int i = beforePrefix.length() - 1; i >= 0; i--) {
            char c = beforePrefix.charAt(i);
            if (c == ']' && end == -1) {
                end = i;
            } else if (c == '[' && end != -1) {
                start = i + 1;
                break;
            }
        }

        if (start != -1 && end != -1 && start < end) {
            return beforePrefix.substring(start, end);
        }

        String[] parts = beforePrefix.split("\\s+");
        for (int i = parts.length - 1; i >= 0; i--) {
            String part = parts[i].replaceAll("[^a-zA-Z0-9_]", "");
            if (part.length() >= 3 && part.length() <= 16) {
                return part;
            }
        }

        return "Someone";
    }
}
