package com.example.agentlab.common;

/**
 * 支持的 LLM 供应商（均为 OpenAI 兼容端点）。
 *
 * <p>当前默认主力为 DeepSeek；切换供应商只需换枚举并设置对应环境变量。
 * Ollama 为本地模型，不需要 API Key，baseUrl 默认 http://localhost:11434/v1。</p>
 *
 * @author guoxiangyue
 */
public enum LlmProvider {

    DEEPSEEK("https://api.deepseek.com/v1", "deepseek-chat"),
    GLM("https://open.bigmodel.cn/api/paas/v4", "glm-4-flash"),
    QWEN("https://dashscope.aliyuncs.com/compatible-mode/v1", "qwen-plus"),
    KIMI("https://api.moonshot.cn/v1", "moonshot-v1-8k"),
    /** Ollama 本地模型服务，OpenAI 兼容端点，不需要 API Key */
    OLLAMA("http://localhost:11434/v1", "qwen2.5");

    private final String baseUrl;
    private final String defaultModel;

    LlmProvider(String baseUrl, String defaultModel) {
        this.baseUrl = baseUrl;
        this.defaultModel = defaultModel;
    }

    /** OpenAI 兼容端点 Base URL。 */
    public String baseUrl() {
        return baseUrl;
    }

    /** 该供应商的默认模型名。 */
    public String defaultModel() {
        return defaultModel;
    }
}
