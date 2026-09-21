package com.example.agentlab.minimalchat;

import com.example.agentlab.common.LlmConfig;
import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实调用 DeepSeek 的集成测试：未设置 DEEPSEEK_API_KEY 时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class HelloLlmIT {

    @Test
    void deepseek_returnsNonBlankAnswer() {
        LlmConfig config = LlmConfig.fromEnv();
        ChatModel model = ModelFactory.createChatModel(config);

        String answer = model.chat("请只回复两个字：你好");

        assertThat(answer).isNotBlank();
        System.out.println("DeepSeek 回复: " + answer);
    }
}
