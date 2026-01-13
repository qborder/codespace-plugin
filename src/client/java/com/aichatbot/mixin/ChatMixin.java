package com.aichatbot.mixin;

import com.aichatbot.OpenAIHandler;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public class ChatMixin {
    private static final Pattern CHAT_PATTERN = Pattern.compile("<([^>]+)>\\s*(.+)");

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onChatMessage(Text message, CallbackInfo ci) {
        try {
            String raw = message.getString();
            Matcher matcher = CHAT_PATTERN.matcher(raw);

            if (matcher.find()) {
                String playerName = matcher.group(1);
                String chatMessage = matcher.group(2);
                OpenAIHandler.handleMessage(playerName, chatMessage);
            }
        } catch (Exception ignored) {
        }
    }
}
