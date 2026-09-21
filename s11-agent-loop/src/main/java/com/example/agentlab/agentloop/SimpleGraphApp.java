package com.example.agentlab.agentloop;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * langgraph4j 冒烟 Demo（与手写 AgentLoop 对比）：
 * 相同的最小流程，另一种实现——状态图：START → greeter → responder → END。
 *
 * <pre>
 *   mvn -q -pl agent-loop exec:java "-Dexec.mainClass=com.example.agentlab.agentloop.SimpleGraphApp"
 * </pre>
  * @author guoxiangyue
 */
public class SimpleGraphApp {

    public static void main(String[] args) throws GraphStateException {
        var compiledGraph = new StateGraph<>(SimpleState.SCHEMA, SimpleState::new)
                .addNode("greeter", node_async(new GreeterNode()))
                .addNode("responder", node_async(new ResponderNode()))
                .addEdge(START, "greeter")
                .addEdge("greeter", "responder")
                .addEdge("responder", END)
                .compile();

        System.out.println("== 图执行轨迹 ==");
        for (var item : compiledGraph.stream(Map.of(SimpleState.MESSAGES_KEY, "begin"))) {
            System.out.println(item);
        }
    }
}
