package com.example.agentlab.hooks;

import com.example.agentlab.agentloop.AgentLoop;
import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * S1.4 Demo：给 Agent 循环挂上 TraceObserver，运行后输出执行轨迹报告。
 *
 * <pre>
 *   mvn -q -pl hooks exec:java "-Dexec.mainClass=com.example.agentlab.hooks.HooksDemo"
 * </pre>
  * @author guoxiangyue
 */
public class HooksDemo {

    public static void main(String[] args) {
        ChatModel model = ModelFactory.createDefaultChatModel();

        // 手写循环 + 注册两个简单工具（配合 S1.1 的 AgentLoop）
        Map<String, AgentLoop.ToolHandler> tools = Map.of(
                "now", ignored -> "当前时间 " + LocalTime.now().withNano(0),
                "echo", args2 -> "你说了: " + args2
        );

        TraceObserver observer = new TraceObserver();
        AgentLoop loop = new AgentLoop(model, tools, 5, List.of(observer));

        String task = "请先用 now 工具告诉我当前时间，然后用 echo 工具把你的回答复述一遍。";
        System.out.println("任务: " + task);

        String answer = loop.run(task);
        System.out.println("Agent: " + answer);
        System.out.println();
        System.out.println(observer.report());
    }
}
