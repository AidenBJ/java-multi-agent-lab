package com.example.agentlab.subagent;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.model.chat.ChatModel;

/**
 * 子 Agent 执行器（对齐参考工程 s06 spawn_subagent）。
 *
 * <p>关键设计：</p>
 * <ul>
 *   <li>子 Agent = 独立 AiServices 实例（全新消息，无共享记忆）——上下文隔离；</li>
 *   <li>只回传最终文本（结论），中间过程全部丢弃；</li>
 *   <li>子 Agent 没有 task 工具——禁止递归 spawn。</li>
 * </ul>
  * @author guoxiangyue
 */
public class SubagentRunner {

    /** 子 Agent 的系统提示：直接完成任务，不要再委派。 */
    public static final String SUB_SYSTEM_PROMPT =
            "你是子 Agent，负责独立完成一个子任务。直接完成任务并给出结论，不要再次委派任务，不要讨论过程。";

    /** 子 Agent 接口（无任何工具——禁止递归）。 */
    public interface SubAgent {
        String execute(@UserMessage String task);
    }

    private final ChatModel model;

    public SubagentRunner(ChatModel model) {
        this.model = model;
    }

    /** 启动一个子 Agent，返回其最终结论。 */
    public String spawn(String task) {
        SubAgent agent = AiServices.builder(SubAgent.class)
                .chatModel(model)
                .systemMessage(SUB_SYSTEM_PROMPT)
                .build();
        return agent.execute(task);
    }
}
