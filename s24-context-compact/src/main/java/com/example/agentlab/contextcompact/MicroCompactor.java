package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * L2 micro_compact：旧工具结果占位（对齐 s08）。
 *
 * <p>只保留最近 keepRecent 条 ToolExecutionResultMessage 的完整内容；
 * 更旧的（且内容超阈值长度）替换为一行占位符——需要时模型可重新执行工具取回。</p>
  * @author guoxiangyue
 */
public class MicroCompactor {

    static final String PLACEHOLDER = "[Earlier tool result compacted. Re-run if needed.]";
    static final int DEFAULT_KEEP_RECENT = 3;
    static final int MIN_LENGTH_TO_REPLACE = 120;

    private final int keepRecent;

    public MicroCompactor(int keepRecent) {
        this.keepRecent = keepRecent;
    }

    public MicroCompactor() {
        this(DEFAULT_KEEP_RECENT);
    }

    public List<ChatMessage> compact(List<ChatMessage> messages) {
        List<ToolExecutionResultMessage> results = messages.stream()
                .filter(m -> m instanceof ToolExecutionResultMessage)
                .map(m -> (ToolExecutionResultMessage) m)
                .toList();

        if (results.size() <= keepRecent) {
            return messages;
        }

        Set<String> keepIds = new HashSet<>();
        results.subList(results.size() - keepRecent, results.size())
                .forEach(r -> keepIds.add(r.id()));

        List<ChatMessage> result = new ArrayList<>(messages.size());
        for (ChatMessage m : messages) {
            if (m instanceof ToolExecutionResultMessage r
                    && !keepIds.contains(r.id())
                    && r.text() != null && r.text().length() > MIN_LENGTH_TO_REPLACE) {
                result.add(new ToolExecutionResultMessage(r.id(), r.toolName(), PLACEHOLDER));
            } else {
                result.add(m);
            }
        }
        return result;
    }
}
