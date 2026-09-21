package com.example.agentlab.common;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

/**
 * 统一模型工厂：把 {@link LlmConfig} 变成可用的 {@link ChatModel}。
 *
 * <p>所有模块统一从这里拿模型，避免各自拼装配置。
 * Ollama 也是 OpenAI 兼容端点，用同一个 {@link OpenAiChatModel} 即可，无需额外处理。</p>
 *
 * @author guoxiangyue
 */
public final class ModelFactory {

    private ModelFactory() {
    }

    /** 按配置构建聊天模型。 */
    public static ChatModel createChatModel(LlmConfig config) {
        return OpenAiChatModel.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .modelName(config.modelName())
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    /** 便捷方法：从环境变量构建默认（DeepSeek）模型。 */
    public static ChatModel createDefaultChatModel() {
        return createChatModel(LlmConfig.fromEnv());
    }

    /** 按配置构建流式聊天模型（S0.2 流式对话使用）。 */
    public static StreamingChatModel createStreamingChatModel(LlmConfig config) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(config.apiKey())
                .baseUrl(config.baseUrl())
                .modelName(config.modelName())
                .build();
    }
}
