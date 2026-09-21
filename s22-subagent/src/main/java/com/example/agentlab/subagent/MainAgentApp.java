package com.example.agentlab.subagent;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;

/**
 * S2.2 Demo A（对齐参考工程 s06）：主 Agent 通过 task 工具 spawn 子 Agent。
 *
 * <p>观察重点：子 Agent 的输出只有结论；主 Agent 上下文不被子 Agent 的中间过程污染。</p>
 *
 * <pre>
 *   mvn -q -pl subagent exec:java "-Dexec.mainClass=com.example.agentlab.subagent.MainAgentApp"
 * </pre>
  * @author guoxiangyue
 */
public class MainAgentApp {

    interface MainAgent {
        String run(@UserMessage String task);
    }

    public static void main(String[] args) {
        ChatModel model = ModelFactory.createDefaultChatModel();

        MainAgent agent = AiServices.builder(MainAgent.class)
                .chatModel(model)
                .systemMessage("你是主 Agent。遇到复杂子任务时调用 task 工具派发给子 Agent，子 Agent 会返回结论；" +
                        "拿到结论后基于它继续完成任务。")
                .tools(new TaskTool(new SubagentRunner(model)))
                .build();

        String task = args.length > 0
                ? String.join(" ", args)
                : "请用 task 工具让子 Agent 调研'Java 多 Agent 框架选型'（返回结论），然后你基于结论给出一段 100 字总结。";

        System.out.println("任务: " + task);
        String answer = agent.run(task);
        System.out.println("=== 主 Agent 最终回答 ===");
        System.out.println(answer);
    }
}
