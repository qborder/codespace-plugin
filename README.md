# AI Chatbot Mod for Minecraft 1.21.1 (Fabric)

A client-side Fabric mod that responds to `!ai` messages in chat using a free AI API.

## How It Works

When anyone in chat (including you) types `!ai <question>`, your Minecraft account will respond with an AI-generated answer.

```
[Player1]: !ai what's the best diamond level?
[You]: Diamonds spawn most at Y=-59 in 1.21. Try branch mining there!
```

## Setup

### 1. Build the Mod

```bash
# In Codespaces terminal:
gradle wrapper
./gradlew build
```

The mod JAR will be in `build/libs/aichatbot-1.0.0.jar`

### 2. Install the Mod

1. Copy `aichatbot-1.0.0.jar` to your `.minecraft/mods` folder
2. Make sure you have [Fabric Loader](https://fabricmc.net/use/installer/) installed
3. Make sure you have [Fabric API](https://modrinth.com/mod/fabric-api) installed

### 3. (Optional) Configure

The mod works out of the box with the free API! But you can customize:

1. Launch Minecraft once with the mod
2. Close Minecraft
3. Open `.minecraft/config/aichatbot.json`

```json
{
  "apiUrl": "https://ai-chat-api-lake.vercel.app/api/chat",
  "apiKey": "sk-aichat-default-key-2024",
  "provider": "chat.gpt-chatbot.ru",
  "model": "chatgpt-4o-latest",
  "prefix": "!ai",
  "systemPrompt": "You are a helpful Minecraft assistant. Keep responses short.",
  "cooldownMs": 2000,
  "maxTokens": 150
}
```

## Available Models

| Model                                 | Provider         |
| ------------------------------------- | ---------------- |
| `chatgpt-4o-latest`                   | OpenAI (default) |
| `gpt-5.1`                             | OpenAI           |
| `gpt-5`                               | OpenAI           |
| `o3-mini`                             | OpenAI           |
| `deepseek-ai/DeepSeek-R1-0528`        | DeepSeek         |
| `anthropic/claude-sonnet-4`           | Anthropic        |
| `google/gemini-2.5-pro-preview-05-06` | Google           |
| `x-ai/grok-4`                         | xAI              |

## Config Options

| Option         | Description                | Default               |
| -------------- | -------------------------- | --------------------- |
| `apiUrl`       | API endpoint               | Free API              |
| `apiKey`       | API key                    | Pre-configured        |
| `provider`     | AI provider                | `chat.gpt-chatbot.ru` |
| `model`        | Model to use               | `chatgpt-4o-latest`   |
| `prefix`       | Trigger prefix             | `!ai`                 |
| `systemPrompt` | AI personality             | Minecraft helper      |
| `cooldownMs`   | Cooldown between responses | 2000ms                |
| `maxTokens`    | Max response length        | 150                   |

## Building from Source

```bash
git clone https://github.com/qborder/codespace-plugin.git
cd codespace-plugin
gradle wrapper
./gradlew build
```
