package com.example.agentlab.memory;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * 记忆整理器（对齐参考工程 s09 consolidate / Dream）：数量达到阈值时，LLM 去重合并。
  * @author guoxiangyue
 */
public class LlmMemoryConsolidator {

    /** AiServices 接口：结构化返回合并后的记忆数组。 */
    public interface Agent {
        @SystemMessage("""
                你是记忆整理员。把多条记忆合并去重：
                1. 删除重复、过时、互相矛盾的条目；
                2. 保留最有价值的信息，可把多条合并为一条；
                3. 保持 name 简短（英文连字符）、description 一句话、body 说明如何应用；
                4. 返回合并后的记忆数组。""")
        List<NewMemory> consolidate(@UserMessage String memories);
    }

    private final Agent agent;

    public LlmMemoryConsolidator(ChatModel model) {
        this.agent = AiServices.builder(Agent.class).chatModel(model).build();
    }

    public List<MemoryRecord> consolidate(List<MemoryRecord> all) {
        StringBuilder sb = new StringBuilder();
        for (MemoryRecord r : all) {
            sb.append("- ").append(r.name()).append(" (").append(r.type().name().toLowerCase())
                    .append("): ").append(r.description()).append("\n  正文: ").append(r.body()).append("\n");
        }
        return agent.consolidate(sb.toString()).stream()
                .map(NewMemory::toRecord)
                .toList();
    }
}
