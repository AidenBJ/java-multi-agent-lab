package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 四层管线编排：顺序固定；token 阈值内只跑 L1–L3，超阈值触发 L4。
 */
class ContextCompactorTest {

    @TempDir
    Path tempDir;

    private ContextCompactor compactor(int tokenThreshold, HistorySummarizer summarizer) {
        return new ContextCompactor(
                new ToolResultBudgetCompactor(200_000, tempDir),
                new SnipCompactor(50, 3),
                new MicroCompactor(3),
                summarizer,
                tokenThreshold);
    }

    private List<ChatMessage> longDialogue(int toolRounds) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage("任务"));
        messages.add(new ToolExecutionResultMessage("0", "read", "初始".repeat(100)));
        for (int i = 1; i <= toolRounds; i++) {
            messages.add(new ToolExecutionResultMessage(String.valueOf(i), "read",
                    "内容".repeat(200)));
        }
        return messages;
    }

    @Test
    void withinThreshold_onlyStructuralLayers_noSummary() {
        List<ChatMessage> messages = longDialogue(5);
        ContextCompactor c = compactor(100_000, m -> failCalled());

        CompactionResult result = c.compact(messages);

        assertThat(result.summarized()).isFalse();
        assertThat(result.layersApplied()).isEqualTo(3);
        // micro：最近 3 个结果原样，更早的占位
        assertThat(((ToolExecutionResultMessage) result.messages().get(1)).text())
                .isEqualTo(MicroCompactor.PLACEHOLDER);
    }

    @Test
    void overThreshold_triggersL4_summary() {
        List<ChatMessage> messages = longDialogue(10);
        ContextCompactor c = compactor(1, m -> "[L4 摘要]");

        CompactionResult result = c.compact(messages);

        assertThat(result.summarized()).isTrue();
        assertThat(result.layersApplied()).isEqualTo(4);
        assertThat(result.messages()).hasSize(1);
        assertThat(((UserMessage) result.messages().get(0)).singleText())
                .contains("[Compacted]").contains("[L4 摘要]");
    }

    private static String failCalled() {
        throw new AssertionError("不应调用 LLM 摘要");
    }
}
