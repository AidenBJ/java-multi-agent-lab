package com.example.agentlab.contextcompact;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * L1 snip_compact：裁中间旧对话 + tool_use/tool_result 配对保护。
 */
class SnipCompactorTest {

    private final SnipCompactor snip = new SnipCompactor(50, 3);

    @Test
    void underThreshold_unchanged() {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            messages.add(new UserMessage("消息 " + i));
        }
        assertThat(snip.compact(messages)).hasSize(40);
    }

    @Test
    void overThreshold_keepsHeadAndTail_addsPlaceholder() {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            messages.add(new UserMessage("消息 " + i));
        }

        List<ChatMessage> result = snip.compact(messages);

        // 头 3 + 1 占位 + 尾 47 = 51
        assertThat(result).hasSize(51);
        // 头部原样
        assertThat(((UserMessage) result.get(0)).singleText()).isEqualTo("消息 0");
        // 中间是占位符
        assertThat(((UserMessage) result.get(3)).singleText()).contains("snipped");
        // 尾部原样
        assertThat(((UserMessage) result.get(50)).singleText()).isEqualTo("消息 99");
    }

    @Test
    void toolUseAndResult_notSplitAcrossCut() {
        // messages[2] = AiMessage(tool request)，messages[3] = ToolResult
        // headEnd=3 时切口压在 ToolResult 上 → 必须推进到 4，不能拆开
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage("初始任务"));
        messages.add(new AiMessage("开始"));
        messages.add(new AiMessage("调用工具",
                List.of(ToolExecutionRequest.builder().id("1").name("read").arguments("{}").build())));
        messages.add(new ToolExecutionResultMessage("1", "read", "工具返回内容"));
        for (int i = 4; i < 100; i++) {
            messages.add(new UserMessage("后续消息 " + i));
        }

        List<ChatMessage> result = snip.compact(messages);

        // ToolResult(id=1) 必须仍与 AiMessage(tool request) 同在头部切口前
        ChatMessage second = result.get(1);
        ChatMessage third = result.get(2);
        ChatMessage fourth = result.get(3);
        assertThat(second).isInstanceOf(AiMessage.class);
        assertThat(third).isInstanceOf(AiMessage.class);
        assertThat(fourth).isInstanceOf(ToolExecutionResultMessage.class);
        assertThat(((ToolExecutionResultMessage) fourth).id()).isEqualTo("1");
    }
}
