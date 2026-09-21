package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * L1 snip_compact：裁掉中间无关的旧对话（对齐 s08）。
 *
 * <p>消息数超阈值 → 保留头部 headKeep（初始上下文）和尾部 max-headKeep（当前工作），
 * 中间裁掉并插入占位符。边界保护：不能把 assistant(tool_use) 和紧跟其后的
 * user(tool_result) 拆开。</p>
  * @author guoxiangyue
 */
public class SnipCompactor {

    private final int maxMessages;
    private final int headKeep;

    public SnipCompactor(int maxMessages, int headKeep) {
        this.maxMessages = maxMessages;
        this.headKeep = headKeep;
    }

    public List<ChatMessage> compact(List<ChatMessage> messages) {
        int len = messages.size();
        if (len <= maxMessages) {
            return messages;
        }

        int headEnd = headKeep;
        int tailStart = len - (maxMessages - headKeep);

        // 边界保护：head 切口处若上一条 AiMessage 带 tool requests，把后续 tool results 推进 head
        while (headEnd < tailStart && messages.get(headEnd) instanceof ToolExecutionResultMessage) {
            headEnd++;
        }
        // 边界保护：tail 切口处若首条是 tool result 且前一条是带 tool requests 的 AiMessage，回退
        if (tailStart > 0 && tailStart < len
                && messages.get(tailStart) instanceof ToolExecutionResultMessage
                && messages.get(tailStart - 1) instanceof AiMessage a
                && a.hasToolExecutionRequests()) {
            tailStart--;
        }

        int snipped = Math.max(0, tailStart - headEnd);
        List<ChatMessage> result = new ArrayList<>(messages.subList(0, headEnd));
        result.add(new UserMessage("[snipped " + snipped + " messages from conversation middle]"));
        result.addAll(messages.subList(tailStart, len));
        return result;
    }
}
