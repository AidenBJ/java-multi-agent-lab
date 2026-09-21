package com.example.agentlab.orchestrator;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Orchestrator-Worker 图离线测试：拆解→fan-out→fan-in→聚合，结果无丢失。
 */
class OrchestratorGraphTest {

    @Test
    void splits_dispatchesAll_workers_aggregates() throws Exception {
        TaskSplitter splitter = task -> List.of("子任务A", "子任务B", "子任务C");
        List<ParallelWorker> workers = List.of(
                stub("w1"), stub("w2"), stub("w3"));
        Aggregator aggregator = (task, results) -> "报告[" + results.size() + "条]";

        OrchestratorGraph graph = new OrchestratorGraph(splitter, workers, aggregator);

        String report = graph.run("调研任务");

        assertThat(report).contains("报告[3条]");
    }

    @Test
    void eachWorkerHandlesItsOwnSubtask() throws Exception {
        TaskSplitter splitter = task -> List.of("市场", "竞品", "用户");
        StringBuilder handled = new StringBuilder();
        List<ParallelWorker> workers = List.of(
                recording("w1", handled), recording("w2", handled), recording("w3", handled));
        Aggregator aggregator = (task, results) -> "ok";

        OrchestratorGraph graph = new OrchestratorGraph(splitter, workers, aggregator);
        graph.run("调研");

        // 每个 worker 都处理到了自己的子任务（w1→市场, w2→竞品, w3→用户）
        assertThat(handled.toString()).contains("w1:市场").contains("w2:竞品").contains("w3:用户");
    }

    static ParallelWorker stub(String id) {
        return new ParallelWorker() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String handle(String subtask, String task) {
                return id + "→" + subtask;
            }
        };
    }

    static ParallelWorker recording(String id, StringBuilder out) {
        return new ParallelWorker() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String handle(String subtask, String task) {
                out.append(id).append(":").append(subtask).append(";");
                return "done";
            }
        };
    }
}
