package com.example.agentlab.memory;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * LLM 记忆选择器（对齐参考工程 s09 的 side-query）：把对话 + 记忆目录
 * （name — description）交给模型，模型返回选中的记忆名数组。
  * @author guoxiangyue
 */
public class LlmMemorySelector implements MemorySelector {

    /** AiServices 接口：结构化返回选中的记忆名数组。 */
    public interface Agent {
        @SystemMessage("""
                你是记忆选择器。根据最近的对话，从记忆目录中选出真正有用的记忆（最多 5 个）。
                不确定就不选。返回记忆的 name 数组，没有相关的返回空数组。""")
        List<String> select(@UserMessage String catalogWithDialogue);
    }

    private final Agent agent;

    public LlmMemorySelector(ChatModel model) {
        this.agent = AiServices.builder(Agent.class).chatModel(model).build();
    }

    @Override
    public List<MemoryRecord> select(List<MemoryRecord> all, String dialogue, int maxItems) {
        StringBuilder catalog = new StringBuilder("最近对话:\n").append(dialogue).append("\n\n记忆目录:\n");
        for (int i = 0; i < all.size(); i++) {
            catalog.append(i).append(": ").append(all.get(i).name()).append(" — ")
                    .append(all.get(i).description()).append("\n");
        }
        List<String> selectedNames = agent.select(catalog.toString());
        return all.stream()
                .filter(r -> selectedNames.contains(r.name()))
                .limit(maxItems)
                .toList();
    }
}
