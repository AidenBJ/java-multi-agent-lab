package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;

/**
 * token 估算（教学版：字符数近似，对齐参考工程"token 用字符数估算"的刻意简化）。
  * @author guoxiangyue
 */
public final class MessageEstimator {

    private MessageEstimator() {
    }

    /** 估算 token：总字符数 / 4（粗近似，够用教学；真实场景上 tokenizer）。 */
    public static int estimate(List<ChatMessage> messages) {
        return totalChars(messages) / 4;
    }

    /** 总字符数（各消息文本长度之和）。 */
    public static int totalChars(List<ChatMessage> messages) {
        int sum = 0;
        for (ChatMessage m : messages) {
            sum += textOf(m).length();
        }
        return sum;
    }

    static String textOf(ChatMessage m) {
        if (m instanceof UserMessage u) {
            return u.singleText() == null ? "" : u.singleText();
        }
        if (m instanceof dev.langchain4j.data.message.AiMessage a) {
            return a.text() == null ? "" : a.text();
        }
        if (m instanceof ToolExecutionResultMessage r) {
            return r.text() == null ? "" : r.text();
        }
        return "";
    }
}
