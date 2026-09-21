package com.example.agentlab.supervisor;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Supervisor 图（LangGraph 官方 Supervisor 模式的 langgraph4j 实现）：
 *
 * <pre>
 *   START → supervisor ──条件边──→ writer / coder / analyst / END(done)
 *                       ↑                          │
 *                       └──────────────────────────┘（worker 干完回 supervisor 继续派）
 * </pre>
 *
 * <p>主管只做路由与协调，真正的活派给专职 worker。</p>
  * @author guoxiangyue
 */
public class SupervisorGraph {

    private final Router router;
    private final Map<String, Worker> workers;
    private final CompiledGraph<SupervisorState> compiled;

    public SupervisorGraph(Router router, List<Worker> workerList) throws Exception {
        this.router = router;
        this.workers = new LinkedHashMap<>();
        for (Worker w : workerList) {
            this.workers.put(w.id(), w);
        }
        this.compiled = build();
    }

    private CompiledGraph<SupervisorState> build() throws Exception {
        StateGraph<SupervisorState> g = new StateGraph<>(SupervisorState.SCHEMA, SupervisorState::new);

        g.addNode("supervisor", node_async((SupervisorState state) -> supervisorStep(state)));
        for (Worker w : workers.values()) {
            g.addNode(w.id(), node_async((SupervisorState state) -> workerStep(w, state)));
        }

        g.addEdge(START, "supervisor");

        // 条件边：主管决定的 route 决定下一个节点
        Map<String, String> routeMap = new HashMap<>();
        routeMap.put("done", END);
        for (String id : workers.keySet()) {
            routeMap.put(id, id);
        }
        g.addConditionalEdges("supervisor", edge_async((SupervisorState state) -> state.route()), routeMap);

        // 每个 worker 干完回 supervisor
        for (String id : workers.keySet()) {
            g.addEdge(id, "supervisor");
        }

        return g.compile();
    }

    private Map<String, Object> supervisorStep(SupervisorState state) {
        RouteDecision decision = router.route(state.task(), state.results());
        return Map.of("route", decision.worker());
    }

    private Map<String, Object> workerStep(Worker worker, SupervisorState state) {
        String result = worker.work(state.task(), state.results());
        return Map.of("results", List.of("[" + worker.id() + "] " + result));
    }

    /** 跑一个任务，返回所有 worker 的累积结果。 */
    public List<String> run(String task) throws Exception {
        Map<String, Object> initial = new HashMap<>();
        initial.put("task", task);
        initial.put("results", new ArrayList<String>());
        SupervisorState finalState = compiled.invoke(initial).get();
        return finalState.results();
    }
}
