package com.example.agentlab.supervisor;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * LLM worker：用角色提示 + 任务上下文调用模型。
  * @author guoxiangyue
 */
public class LlmWorker implements Worker {

    interface Agent {
        @SystemMessage("你是 {{role}}。完成分配给你的部分，只输出你负责的内容。")
        String work(@UserMessage String taskAndResults);
    }

    private final String id;
    private final String role;
    private final Agent agent;

    public LlmWorker(String id, String role, ChatModel model) {
        this.id = id;
        this.role = role;
        this.agent = AiServices.builder(Agent.class).chatModel(model).build();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String role() {
        return role;
    }

    @Override
    public String work(String task, List<String> results) {
        String context = "任务: " + task + "\n已有结果: " + String.join(" | ", results);
        return agent.work(context);
    }
}
