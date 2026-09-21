package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * L3 tool_result_budget：大结果落盘（对齐 s08）。
 *
 * <p>所有 ToolExecutionResultMessage 文本总大小超阈值 → 按大小降序从最大的开始落盘到
 * {@code .task-outputs/tool-results/}，上下文只留 persisted 标记 + 预览。
 * <b>此层必须最先跑</b>（micro 会先把旧结果替换成占位符）。</p>
  * @author guoxiangyue
 */
public class ToolResultBudgetCompactor {

    static final int DEFAULT_MAX_BYTES = 200_000;
    static final int PREVIEW_CHARS = 2000;

    private final int maxBytes;
    private final Path outputDir;

    public ToolResultBudgetCompactor(int maxBytes, Path outputDir) {
        this.maxBytes = maxBytes;
        this.outputDir = outputDir;
    }

    public List<ChatMessage> compact(List<ChatMessage> messages) {
        List<ToolExecutionResultMessage> results = messages.stream()
                .filter(m -> m instanceof ToolExecutionResultMessage)
                .map(m -> (ToolExecutionResultMessage) m)
                .toList();

        int total = results.stream().mapToInt(r -> r.text() == null ? 0 : r.text().length()).sum();
        if (total <= maxBytes) {
            return messages;
        }

        // 从最大的开始落盘，直到总量降到阈值内
        List<ToolExecutionResultMessage> sorted = new ArrayList<>(results);
        sorted.sort(Comparator.comparingInt(
                (ToolExecutionResultMessage r) -> r.text() == null ? 0 : r.text().length()).reversed());

        java.util.Set<String> persistedIds = new java.util.HashSet<>();
        for (ToolExecutionResultMessage r : sorted) {
            if (total <= maxBytes || r.text() == null) {
                break;
            }
            String fileName = "tool-" + r.id() + ".txt";
            String preview = r.text().length() > PREVIEW_CHARS
                    ? r.text().substring(0, PREVIEW_CHARS) : r.text();
            String marker = "[persisted-output: .task-outputs/tool-results/" + fileName + "] preview: " + preview;
            try {
                java.nio.file.Files.createDirectories(outputDir);
                java.nio.file.Files.writeString(outputDir.resolve(fileName), r.text(),
                        StandardCharsets.UTF_8);
            } catch (java.io.IOException e) {
                throw new IllegalStateException("落盘工具结果失败: " + e.getMessage(), e);
            }
            total -= r.text().length() - marker.length();
            persistedIds.add(r.id());
        }

        if (persistedIds.isEmpty()) {
            return messages;
        }

        List<ChatMessage> result = new ArrayList<>(messages.size());
        for (ChatMessage m : messages) {
            if (m instanceof ToolExecutionResultMessage r && persistedIds.contains(r.id())) {
                String preview = r.text().length() > PREVIEW_CHARS
                        ? r.text().substring(0, PREVIEW_CHARS) : r.text();
                result.add(new ToolExecutionResultMessage(r.id(), r.toolName(),
                        "[persisted-output: .task-outputs/tool-results/tool-" + r.id()
                                + ".txt] preview: " + preview));
            } else {
                result.add(m);
            }
        }
        return result;
    }
}
