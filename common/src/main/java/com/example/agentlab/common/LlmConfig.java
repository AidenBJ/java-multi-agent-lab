package com.example.agentlab.common;

import java.util.Objects;

/**
 * LLM 连接配置（不可变）。
 *
 * @param provider 供应商
 * @param apiKey   API Key
 * @param baseUrl  OpenAI 兼容端点 Base URL
 * @param modelName 模型名
 * @author guoxiangyue
 */
public record LlmConfig(LlmProvider provider, String apiKey, String baseUrl, String modelName) {

    public LlmConfig {
        Objects.requireNonNull(provider, "provider");
        apiKey = requireNonBlank(apiKey, "apiKey");
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(modelName, "modelName");
    }

    /**
     * 自动选择供应商：读环境变量 LLM_PROVIDER，默认 DEEPSEEK。
     * <p>例如：set LLM_PROVIDER=ollama 就用本地 Ollama；不设就用 DeepSeek。
     * 系统环境变量优先；.env 文件作为兜底。</p>
     */
    public static LlmConfig fromEnv() {
        String providerName = DotEnv.get("LLM_PROVIDER");
        if (providerName == null || providerName.isBlank()) {
            return fromEnv(LlmProvider.DEEPSEEK);
        }
        return fromEnv(LlmProvider.valueOf(providerName.trim().toUpperCase()));
    }

    /**
     * 从环境变量构建指定供应商的配置。
     * <p>Ollama 不需要 API Key，自动用 "ollama" 占位；其他供应商必须设对应环境变量。
     * 系统环境变量优先；.env 文件作为兜底。</p>
     */
    public static LlmConfig fromEnv(LlmProvider provider) {
        // Ollama 是本地服务，不需要 API Key
        if (provider == LlmProvider.OLLAMA) {
            // 允许通过 OLLAMA_BASE_URL 和 OLLAMA_MODEL 覆盖默认值
            String baseUrl = DotEnv.get("OLLAMA_BASE_URL");
            if (baseUrl == null || baseUrl.isBlank()) {
                baseUrl = provider.baseUrl();
            }
            String model = DotEnv.get("OLLAMA_MODEL");
            if (model == null || model.isBlank()) {
                model = provider.defaultModel();
            }
            return new LlmConfig(provider, "ollama", baseUrl, model);
        }
        String envKey = switch (provider) {
            case DEEPSEEK -> "DEEPSEEK_API_KEY";
            case GLM -> "ZHIPU_API_KEY";
            case QWEN -> "DASHSCOPE_API_KEY";
            case KIMI -> "MOONSHOT_API_KEY";
            case OLLAMA -> "OLLAMA_API_KEY";
        };
        String apiKey = DotEnv.get(envKey);
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "缺少环境变量 " + envKey + "（供应商 " + provider.name() + "）。"
                            + "请设置后重试，参考工程根目录 .env.example");
        }
        return new LlmConfig(provider, apiKey, provider.baseUrl(), provider.defaultModel());
    }

    private static String requireNonBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        return value;
    }
}
