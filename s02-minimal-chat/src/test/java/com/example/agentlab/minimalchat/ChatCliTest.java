package com.example.agentlab.minimalchat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 窗口记忆纯逻辑测试：不依赖任何网络与模型。
 */
class ChatCliTest {

    @Test
    void windowMemory_keepsOnlyLatestMessages() {
        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(3);

        memory.add(new UserMessage("m1"));
        memory.add(new AiMessage("a1"));
        memory.add(new UserMessage("m2"));
        memory.add(new AiMessage("a2"));
        memory.add(new UserMessage("m3"));

        // 窗口上限 3：m1/a1 应被丢弃，最终保留 [m2, a2, m3]
        assertThat(memory.messages())
                .hasSize(3)
                .extracting(ChatCliTest::messageText)
                .containsExactly("m2", "a2", "m3");
    }

    @Test
    void windowMemory_clearEmptiesHistory() {
        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(10);
        memory.add(new UserMessage("hi"));
        memory.clear();
        assertThat(memory.messages()).isEmpty();
    }

    static String messageText(ChatMessage m) {
        if (m instanceof UserMessage u) {
            return u.singleText();
        }
        if (m instanceof AiMessage a) {
            return a.text();
        }
        return m.type().name();
    }
}
