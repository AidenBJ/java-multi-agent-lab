package com.example.agentlab.subagent;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * S2.2 Demo C：langgraph4j 子图——父图把"子图"当普通节点，子图有自己独立的图与状态空间。
 *
 * <pre>
 *   mvn -q -pl subagent exec:java "-Dexec.mainClass=com.example.agentlab.subagent.SubgraphDemo"
 * </pre>
  * @author guoxiangyue
 */
public class SubgraphDemo {

    public static void main(String[] args) throws Exception {
        // 1. 子图：一个 worker 节点，接收 in，产出 out 并追加消息
        var workerGraph = new StateGraph<>(WorkState.SCHEMA, WorkState::new)
                .addNode("worker", node_async(state -> Map.of(
                        WorkState.MESSAGES, "子图处理: " + state.<String>value(WorkState.IN).orElse("?"),
                        WorkState.OUT, "完成:" + state.<String>value(WorkState.IN).orElse("?"))))
                .addEdge(START, "worker")
                .addEdge("worker", END)
                .compile();

        // 2. 父图：子图作为节点（addNode 接受 CompiledGraph）
        var parentGraph = new StateGraph<>(WorkState.SCHEMA, WorkState::new)
                .addNode("sub", workerGraph)
                .addEdge(START, "sub")
                .addEdge("sub", END)
                .compile();

        System.out.println("== 父图执行（子图作为节点）==");
        WorkState result = parentGraph.invoke(Map.of(WorkState.IN, "任务A")).get();

        System.out.println("out: " + result.out().orElse("?"));
        System.out.println("消息轨迹: " + result.messages());
    }
}
