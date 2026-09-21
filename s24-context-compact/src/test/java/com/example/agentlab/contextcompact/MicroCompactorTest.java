package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * L2 micro_compact：旧工具结果占位，只保留最近 keepRecent 条完整内容。
 */
class MicroCompactorTest {

    private final MicroCompactor micro = new MicroCompactor(3);

    @Test
    void keepsRecent_replacesOldLongResults() {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messages.add(new ToolExecutionResultMessage(String.valueOf(i), "read",
                    "大内容".repeat(100)));
        }

        List<ChatMessage> result = micro.compact(messages);

        // 最近 3 条（id 2,3,4）原样；前 2 条（id 0,1）变占位符
        assertThat(((ToolExecutionResultMessage) result.get(0)).text())
                .isEqualTo(MicroCompactor.PLACEHOLDER);
        assertThat(((ToolExecutionResultMessage) result.get(1)).text())
                .isEqualTo(MicroCompactor.PLACEHOLDER);
        assertThat(((ToolExecutionResultMessage) result.get(4)).text()).contains("大内容");
    }

    @Test
    void shortResults_keptAsIs() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ToolExecutionResultMessage("0", "read", "短"));
        messages.add(new UserMessage("继续"));
        messages.add(new ToolExecutionResultMessage("1", "read", "另一段短"));

        List<ChatMessage> result = micro.compact(messages);

        // 不足 keepRecent 条，且短文本不替换
        assertThat(result).hasSize(3);
    }

    @Test
    void noChangeWhenFewResults() {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ToolExecutionResultMessage("0", "read", "大内容".repeat(100)));
        messages.add(new ToolExecutionResultMessage("1", "read", "另一段".repeat(100)));

        assertThat(micro.compact(messages)).hasSize(2);
    }
}
