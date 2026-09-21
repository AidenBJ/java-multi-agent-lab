package com.example.agentlab.tooluse;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实调用 DeepSeek 验证"模型能自主选择工具"：未设置 DEEPSEEK_API_KEY 时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class ToolCallIT {

    interface Assistant {
        String chat(String userMessage);
    }

    @Test
    void model_picksCalculatorTool_automatically() {
        ChatModel model = ModelFactory.createDefaultChatModel();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(new CalculatorTool())
                .build();

        String answer = assistant.chat("请计算 7*8，只回答结果数字");

        assertThat(answer.trim()).isEqualTo("56");
    }
}
