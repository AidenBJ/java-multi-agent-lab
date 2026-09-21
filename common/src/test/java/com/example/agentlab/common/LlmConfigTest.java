package com.example.agentlab.common;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * LlmConfig 纯单元测试：不发起任何网络请求。
 */
class LlmConfigTest {

    @Test
    void deepseekProvider_hasOpenAiCompatibleEndpoint() {
        assertThat(LlmProvider.DEEPSEEK.baseUrl()).isEqualTo("https://api.deepseek.com/v1");
        assertThat(LlmProvider.DEEPSEEK.defaultModel()).isEqualTo("deepseek-chat");
    }

    @Test
    void config_validatesRequiredFields() {
        assertThatThrownBy(() -> new LlmConfig(null, "k", "u", "m"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LlmConfig(LlmProvider.DEEPSEEK, "  ", "u", "m"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromEnv_withoutKey_throwsHelpfulError() {
        // 本机若已配置 key（系统环境变量或 .env 文件），则跳过该缺失场景测试
        String key = DotEnv.get("DEEPSEEK_API_KEY");
        Assumptions.assumeTrue(key == null || key.isBlank(), "本机已设置 DEEPSEEK_API_KEY（系统环境变量或 .env），跳过缺失 key 场景");

        IllegalStateException ex = assertThrows(IllegalStateException.class, LlmConfig::fromEnv);
        assertThat(ex.getMessage()).contains("DEEPSEEK_API_KEY");
    }

    @Test
    void fromEnv_usesProviderDefaults() {
        LlmConfig config = new LlmConfig(LlmProvider.DEEPSEEK, "test-key",
                LlmProvider.DEEPSEEK.baseUrl(), LlmProvider.DEEPSEEK.defaultModel());
        assertThat(config.provider()).isEqualTo(LlmProvider.DEEPSEEK);
        assertThat(config.apiKey()).isEqualTo("test-key");
        assertThat(config.modelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void ollamaProvider_hasLocalEndpoint_andNoKeyNeeded() {
        // Ollama 本地服务，不需要 API Key
        assertThat(LlmProvider.OLLAMA.baseUrl()).isEqualTo("http://localhost:11434/v1");
        LlmConfig config = LlmConfig.fromEnv(LlmProvider.OLLAMA);
        assertThat(config.provider()).isEqualTo(LlmProvider.OLLAMA);
        assertThat(config.apiKey()).isEqualTo("ollama");
        assertThat(config.baseUrl()).isEqualTo("http://localhost:11434/v1");
        assertThat(config.modelName()).isEqualTo("qwen2.5");
    }
}
