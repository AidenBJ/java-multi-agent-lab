package com.example.agentlab.memory;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * LLM 记忆提取器：把对话（含已有记忆）交给模型，模型判定哪些值得长期记住。
  * @author guoxiangyue
 */
public class LlmMemoryExtractor implements MemoryExtractor {

    /** AiServices 接口：结构化返回新记忆数组。 */
    public interface Agent {
        @SystemMessage("""
                你是记忆提取器。从对话中提取值得长期记住的信息，只保留：
                - 用户稳定偏好（type: user）
                - 反复出现的反馈/约束（type: feedback）
                - 项目背景事实（type: project）
                - 常用入口/排查线索（type: reference）
                规则：
                1. 若信息已被"已有记忆"覆盖，不要重复提取；
                2. 没有新信息时返回空数组；
                3. 每个记忆的 name 简短（英文连字符）、description 一句话、body 说明如何应用。""")
        List<NewMemory> extract(@UserMessage String dialogueWithExisting);
    }

    private final Agent agent;

    public LlmMemoryExtractor(ChatModel model) {
        this.agent = AiServices.builder(Agent.class).chatModel(model).build();
    }

    @Override
    public List<NewMemory> extract(String dialogueWithExisting) {
        return agent.extract(dialogueWithExisting);
    }
}
