package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;

/**
 * 四层压缩管线编排（对齐 s08）：<b>便宜的先跑，贵的后跑</b>。
 *
 * <p>顺序固定，不能换：L3 budget（大结果落盘）→ L1 snip（裁中间）→ L2 micro（旧结果占位）
 * → L4 auto（LLM 摘要，仅当 token 仍超阈值）。</p>
  * @author guoxiangyue
 */
public class ContextCompactor {

    private final ToolResultBudgetCompactor budget;
    private final SnipCompactor snip;
    private final MicroCompactor micro;
    private final HistorySummarizer summarizer;
    private final int tokenThreshold;

    public ContextCompactor(ToolResultBudgetCompactor budget, SnipCompactor snip,
                            MicroCompactor micro, HistorySummarizer summarizer, int tokenThreshold) {
        this.budget = budget;
        this.snip = snip;
        this.micro = micro;
        this.summarizer = summarizer;
        this.tokenThreshold = tokenThreshold;
    }

    public CompactionResult compact(List<ChatMessage> messages) {
        int before = MessageEstimator.totalChars(messages);
        int layers = 0;

        // 顺序不能换：budget 必须最先（micro 会把旧结果替换成占位符）
        List<ChatMessage> current = budget.compact(messages);
        layers++;
        current = snip.compact(current);
        layers++;
        current = micro.compact(current);
        layers++;

        // 纯结构操作仍不够 → LLM 全量摘要（1 次 API）
        boolean summarized = false;
        if (MessageEstimator.estimate(current) > tokenThreshold) {
            current = List.of(new UserMessage("[Compacted]\n\n" + summarizer.summarize(current)));
            layers++;
            summarized = true;
        }

        return new CompactionResult(current, layers, before,
                MessageEstimator.totalChars(current), summarized);
    }
}
