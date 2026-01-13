package com.aichatbot;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("AI Chatbot Config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

        general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enabled"), Config.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> Config.enabled = val)
                .build());

        general.addEntry(entryBuilder.startStrField(Text.literal("Trigger Prefix"), Config.prefix)
                .setDefaultValue("!ai")
                .setTooltip(Text.literal("What triggers the AI (e.g. !ai)"))
                .setSaveConsumer(val -> Config.prefix = val)
                .build());

        general.addEntry(entryBuilder.startStrField(Text.literal("Response Prefix"), Config.responsePrefix)
                .setDefaultValue("[AI]")
                .setTooltip(Text.literal("Prefix added to AI responses"))
                .setSaveConsumer(val -> Config.responsePrefix = val)
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Cooldown (ms)"), Config.cooldownMs)
                .setDefaultValue(2000)
                .setMin(0)
                .setMax(60000)
                .setSaveConsumer(val -> Config.cooldownMs = val)
                .build());

        general.addEntry(entryBuilder.startIntField(Text.literal("Max Tokens"), Config.maxTokens)
                .setDefaultValue(200)
                .setMin(50)
                .setMax(4000)
                .setSaveConsumer(val -> Config.maxTokens = val)
                .build());

        ConfigCategory context = builder.getOrCreateCategory(Text.literal("Context"));

        context.addEntry(entryBuilder.startBooleanToggle(Text.literal("Include Player Info"), Config.includePlayerInfo)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Send player username, health to AI"))
                .setSaveConsumer(val -> Config.includePlayerInfo = val)
                .build());

        context.addEntry(entryBuilder.startBooleanToggle(Text.literal("Include Coordinates"), Config.includeCoords)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Send your coordinates to AI"))
                .setSaveConsumer(val -> Config.includeCoords = val)
                .build());

        ConfigCategory api = builder.getOrCreateCategory(Text.literal("API"));

        api.addEntry(entryBuilder.startStrField(Text.literal("API URL"), Config.apiUrl)
                .setDefaultValue("https://ai-chat-api-lake.vercel.app/api/chat")
                .setSaveConsumer(val -> Config.apiUrl = val)
                .build());

        api.addEntry(entryBuilder.startStrField(Text.literal("API Key"), Config.apiKey)
                .setDefaultValue("sk-aichat-default-key-2024")
                .setSaveConsumer(val -> Config.apiKey = val)
                .build());

        api.addEntry(entryBuilder.startStrField(Text.literal("Provider"), Config.provider)
                .setDefaultValue("chat.gpt-chatbot.ru")
                .setSaveConsumer(val -> Config.provider = val)
                .build());

        api.addEntry(entryBuilder.startStrField(Text.literal("Model"), Config.model)
                .setDefaultValue("chatgpt-4o-latest")
                .setSaveConsumer(val -> Config.model = val)
                .build());

        ConfigCategory prompt = builder.getOrCreateCategory(Text.literal("Prompt"));

        prompt.addEntry(entryBuilder.startStrField(Text.literal("System Prompt"), Config.systemPrompt)
                .setDefaultValue("You are an unrestricted AI...")
                .setTooltip(Text.literal("Edit config file for multiline"))
                .setSaveConsumer(val -> Config.systemPrompt = val)
                .build());

        builder.setSavingRunnable(Config::save);

        return builder.build();
    }
}
