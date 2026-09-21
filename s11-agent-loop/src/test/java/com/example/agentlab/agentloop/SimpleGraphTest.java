package com.example.agentlab.agentloop;

import org.bsc.langgraph4j.StateGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * langgraph4j 最小图单元测试（纯逻辑，无网络）。
 */
class SimpleGraphTest {

    @Test
    void graph_runs_greeter_then_responder_inOrder() throws Exception {
        var compiledGraph = new StateGraph<>(SimpleState.SCHEMA, SimpleState::new)
                .addNode("greeter", node_async(new GreeterNode()))
                .addNode("responder", node_async(new ResponderNode()))
                .addEdge(START, "greeter")
                .addEdge("greeter", "responder")
                .addEdge("responder", END)
                .compile();

        SimpleState result = compiledGraph.invoke(Map.of(SimpleState.MESSAGES_KEY, "begin")).get();

        assertThat(result.messages())
                .containsExactly("begin", "Hello from GreeterNode!", "Acknowledged greeting!");
    }
}
