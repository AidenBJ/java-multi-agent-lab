package com.example.agentlab.subagent;

import org.bsc.langgraph4j.StateGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * langgraph4j 子图离线测试：父图把子图作为节点，子图状态更新回流父图。
 */
class SubgraphDemoTest {

    @Test
    void parentGraph_runsSubgraph_asNode() throws Exception {
        var workerGraph = new StateGraph<>(WorkState.SCHEMA, WorkState::new)
                .addNode("worker", node_async(state -> Map.of(
                        WorkState.MESSAGES, "子图处理: " + state.<String>value(WorkState.IN).orElse("?"),
                        WorkState.OUT, "完成:" + state.<String>value(WorkState.IN).orElse("?"))))
                .addEdge(START, "worker")
                .addEdge("worker", END)
                .compile();

        var parentGraph = new StateGraph<>(WorkState.SCHEMA, WorkState::new)
                .addNode("sub", workerGraph)
                .addEdge(START, "sub")
                .addEdge("sub", END)
                .compile();

        WorkState result = parentGraph.invoke(Map.of(WorkState.IN, "任务A")).get();

        // 子图产出回流父图：out 可见、消息追加
        assertThat(result.out()).contains("完成:任务A");
        assertThat(result.messages()).containsExactly("子图处理: 任务A");
    }
}
