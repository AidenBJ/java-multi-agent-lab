package com.example.agentlab.contextcompact;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * LLM 历史摘要器（对齐 s08 的 compact_history）：文本只输出摘要，禁止调工具。
  * @author guoxiangyue
 */
public class LlmHistorySummarizer implements HistorySummarizer {

    interface Agent {
        @SystemMessage("""
                你是对话历史摘要器。把对话历史压缩成一段摘要，必须保留：
                当前目标、重要发现、已完成或已修改的内容、剩余工作、用户约束与偏好。
                只输出摘要文本，绝对不要调用任何工具。""")
        String summarize(@UserMessage String dialogue);
    }

    private final Agent agent;

    public LlmHistorySummarizer(ChatModel model) {
        this.agent = AiServices.builder(Agent.class).chatModel(model).build();
    }

    @Override
    public String summarize(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        for (ChatMessage m : messages) {
            sb.append(MessageEstimator.textOf(m)).append("\n");
        }
        return agent.summarize(sb.toString());
    }
}
