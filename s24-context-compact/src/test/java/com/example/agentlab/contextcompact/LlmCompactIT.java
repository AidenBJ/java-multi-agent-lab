package com.example.agentlab.contextcompact;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实调用 DeepSeek 验证 L4 LLM 摘要：未设置 DEEPSEEK_API_KEY 时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class LlmCompactIT {

    @Test
    void llmSummarizer_producesSummary() {
        ChatModel model = ModelFactory.createDefaultChatModel();
        LlmHistorySummarizer summarizer = new LlmHistorySummarizer(model);

        List<ChatMessage> dialogue = List.of(
                new UserMessage("帮我重构 auth 模块"),
                new ToolExecutionResultMessage("0", "read",
                        "AuthController.java 共 300 行，登录逻辑在 authenticate 方法中"),
                new UserMessage("记住：新接口必须加 JWT 校验"));

        String summary = summarizer.summarize(dialogue);

        assertThat(summary).isNotBlank();
        System.out.println("摘要: " + summary);
    }
}
