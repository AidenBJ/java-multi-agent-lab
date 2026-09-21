package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * L3 tool_result_budget：大结果落盘 + 上下文留 persisted 标记与预览。
 */
class ToolResultBudgetCompactorTest {

    @TempDir
    Path tempDir;

    @Test
    void belowThreshold_noChange() {
        ToolResultBudgetCompactor budget = new ToolResultBudgetCompactor(200_000, tempDir);
        List<ChatMessage> messages = List.of(
                new ToolExecutionResultMessage("0", "read", "小内容"));

        assertThat(budget.compact(messages)).hasSize(1);
    }

    @Test
    void overThreshold_persistsLargest_first() {
        // 4 个 100_000 字符的结果，阈值 200_000 → 最大的两个落盘
        ToolResultBudgetCompactor budget = new ToolResultBudgetCompactor(200_000, tempDir);
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            messages.add(new ToolExecutionResultMessage(String.valueOf(i), "read",
                    "内容".repeat(50_000)));
        }

        List<ChatMessage> result = budget.compact(messages);

        // 前三个（id 0,1,2）被落盘 → 占位符；最后一个（id 3）仍超阈值前已停 → 原样
        ToolExecutionResultMessage r0 = (ToolExecutionResultMessage) result.get(0);
        ToolExecutionResultMessage r2 = (ToolExecutionResultMessage) result.get(2);
        ToolExecutionResultMessage r3 = (ToolExecutionResultMessage) result.get(3);
        assertThat(r0.text()).contains("[persisted-output: .task-outputs/tool-results/tool-0.txt]");
        assertThat(r0.text()).contains("preview:");
        assertThat(r2.text()).contains("[persisted-output:");
        assertThat(r3.text()).startsWith("内容");
        // 文件确实落盘
        assertThat(tempDir.resolve("tool-0.txt")).exists();
    }

    @Test
    void persistedFileContainsFullContent() throws Exception {
        ToolResultBudgetCompactor budget = new ToolResultBudgetCompactor(100, tempDir);
        List<ChatMessage> messages = List.of(
                new ToolExecutionResultMessage("big", "read", "A".repeat(500)));

        budget.compact(messages);

        String file = java.nio.file.Files.readString(tempDir.resolve("tool-big.txt"));
        assertThat(file).hasSize(500);
    }
}
