package com.example.agentlab.orchestrator;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Orchestrator-Worker 图（LangGraph 官方模式）：
 *
 * <pre>
 *   START → orchestrator(拆任务) ──fan-out──→ worker0 / worker1 / worker2
 *                                                    │
 *                                              fan-in ↓
 *                                              aggregator(聚合) → END
 * </pre>
 *
 * <p>orchestrator 拆出 N 个子任务 → 每个 worker 并行处理一个 → aggregator 全部到齐后汇总。</p>
 *
 * @author guoxiangyue
 */
public class OrchestratorGraph {

    /** 任务拆解器 */
    private final TaskSplitter splitter;
    /** 并行 worker 列表 */
    private final List<ParallelWorker> workers;
    /** 结果聚合器 */
    private final Aggregator aggregator;
    /** 编译后的图 */
    private final CompiledGraph<OrchestratorState> compiled;

    /**
     * 构造图：注入拆解器、worker 列表、聚合器，自动组装图。
     *
     * @param splitter  任务拆解器
     * @param workers   并行 worker 列表
     * @param aggregator 结果聚合器
     */
    public OrchestratorGraph(TaskSplitter splitter, List<ParallelWorker> workers, Aggregator aggregator) throws Exception {
        this.splitter = splitter;
        this.workers = List.copyOf(workers);
        this.aggregator = aggregator;
        this.compiled = build();
    }

    /**
     * 组装图：orchestrator 节点 + N 个 worker 节点 + aggregator 节点。
     * fan-out：orchestrator → 所有 worker；fan-in：所有 worker → aggregator。
     */
    private CompiledGraph<OrchestratorState> build() throws Exception {
        // 用状态 Schema 和构造器创建状态图
        StateGraph<OrchestratorState> g = new StateGraph<>(OrchestratorState.SCHEMA, OrchestratorState::new);

        // orchestrator 节点：调拆解器把大任务拆成子任务列表
        g.addNode("orchestrator", node_async((OrchestratorState s) ->
                Map.of("subtasks", new ArrayList<>(splitter.split(s.task())))));

        // 为每个 worker 建一个节点：处理对应下标的子任务
        for (int i = 0; i < workers.size(); i++) {
            final int idx = i;
            ParallelWorker w = workers.get(i);
            g.addNode("worker_" + w.id(), node_async((OrchestratorState s) -> {
                List<String> subs = s.subtasks();
                // 第 idx 个 worker 处理第 idx 个子任务
                String subtask = idx < subs.size() ? subs.get(idx) : "(无子任务)";
                return Map.of("partialResults", List.of("[" + w.id() + "] " + w.handle(subtask, s.task())));
            }));
        }

        // aggregator 节点：把所有 partialResults 聚合成最终报告
        g.addNode("aggregator", node_async((OrchestratorState s) ->
                Map.of("finalReport", aggregator.aggregate(s.task(), s.partialResults()))));

        // 从 START 进 orchestrator
        g.addEdge(START, "orchestrator");

        // fan-out：orchestrator → 所有 worker；fan-in：所有 worker → aggregator
        for (ParallelWorker w : workers) {
            g.addEdge("orchestrator", "worker_" + w.id());
            g.addEdge("worker_" + w.id(), "aggregator");
        }

        g.addEdge("aggregator", END);

        return g.compile();
    }

    /**
     * 执行图：传入大任务，返回最终报告。
     *
     * @param task 大任务
     * @return 聚合后的最终报告
     */
    public String run(String task) throws Exception {
        // 初始化状态：任务 + 空子任务列表 + 空部分结果列表
        Map<String, Object> initial = new HashMap<>();
        initial.put("task", task);
        initial.put("subtasks", new ArrayList<String>());
        initial.put("partialResults", new ArrayList<String>());
        return compiled.invoke(initial).get().finalReport();
    }
}
