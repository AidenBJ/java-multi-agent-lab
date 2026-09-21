package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 应急 reactive_compact：保留最近 keepTail 条，只总结较早历史。
 */
class ReactiveCompactorTest {

    @Test
    void summarizesEarly_keepsRecentTail() {
        HistorySummarizer stub = messages -> "[摘要] " + messages.size() + " 条已总结";
        ReactiveCompactor reactive = new ReactiveCompactor(stub, 5);

        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            messages.add(new UserMessage("消息 " + i));
        }

        List<ChatMessage> result = reactive.compact(messages);

        // 1 条摘要 + 5 条尾部
        assertThat(result).hasSize(6);
        assertThat(((UserMessage) result.get(0)).singleText())
                .contains("[Reactive compact]").contains("5 条已总结");
        // 尾部原样保留
        assertThat(((UserMessage) result.get(5)).singleText()).isEqualTo("消息 9");
    }

    @Test
    void shortHistory_noOp() {
        HistorySummarizer stub = messages -> "[摘要]";
        ReactiveCompactor reactive = new ReactiveCompactor(stub, 5);

        List<ChatMessage> messages = List.of(new UserMessage("hi"));

        List<ChatMessage> result = reactive.compact(messages);

        assertThat(result).hasSize(1);
    }

    @Test
    void toolResultAtBoundary_pushedIntoTail() {
        HistorySummarizer stub = messages -> "[摘要]";
        ReactiveCompactor reactive = new ReactiveCompactor(stub, 3);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage("早"));
        messages.add(new ToolExecutionResultMessage("old", "read", "旧结果"));
        // tailStart=7 附近：构造 8 条，尾部 3 条含一个 ToolResult
        for (int i = 2; i < 7; i++) {
            messages.add(new UserMessage("中间 " + i));
        }
        messages.add(new ToolExecutionResultMessage("recent", "read", "新结果"));
        messages.add(new UserMessage("尾 1"));
        messages.add(new UserMessage("尾 2"));

        List<ChatMessage> result = reactive.compact(messages);

        // ToolResult(recent) 必须保留在尾部
        assertThat(result.stream().filter(m -> m instanceof ToolExecutionResultMessage
                && ((ToolExecutionResultMessage) m).id().equals("recent")).count()).isEqualTo(1);
    }
}
