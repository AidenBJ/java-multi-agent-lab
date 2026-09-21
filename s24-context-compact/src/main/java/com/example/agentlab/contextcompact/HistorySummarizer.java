package com.example.agentlab.contextcompact;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * L4 历史摘要器：把对话历史压成一段文字。
  * @author guoxiangyue
 */
public interface HistorySummarizer {

    /** 保留：当前目标、重要发现、已完成/已修改内容、剩余工作、用户约束与偏好。 */
    String summarize(List<ChatMessage> messages);
}
