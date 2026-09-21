package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 应急 reactive_compact（对齐 s08）：API 报 prompt_too_long 时触发——
 * 压缩更激进，但只总结较早历史，保留最近 keepTail 条原始消息。
  * @author guoxiangyue
 */
public class ReactiveCompactor {

    static final int DEFAULT_KEEP_TAIL = 5;

    private final HistorySummarizer summarizer;
    private final int keepTail;

    public ReactiveCompactor(HistorySummarizer summarizer, int keepTail) {
        this.summarizer = summarizer;
        this.keepTail = keepTail;
    }

    public List<ChatMessage> compact(List<ChatMessage> messages) {
        int len = messages.size();
        int tailStart = Math.max(0, len - keepTail);

        // 边界保护：不把 tool_use 和 tool_result 拆开
        if (tailStart > 0 && tailStart < len
                && messages.get(tailStart) instanceof ToolExecutionResultMessage
                && messages.get(tailStart - 1) instanceof AiMessage a
                && a.hasToolExecutionRequests()) {
            tailStart--;
        }

        List<ChatMessage> result = new ArrayList<>(len - tailStart + 1);
        if (tailStart > 0) {
            String summary = summarizer.summarize(messages.subList(0, tailStart));
            result.add(new UserMessage("[Reactive compact]\n\n" + summary));
        }
        result.addAll(messages.subList(tailStart, len));
        return result;
    }
}
