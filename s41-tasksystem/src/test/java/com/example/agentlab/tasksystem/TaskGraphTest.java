package com.example.agentlab.tasksystem;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 任务图离线测试：DAG 依赖、就绪判定、状态流转。
 */
class TaskGraphTest {

    @Test
    void readyTasks_respectDagDependencies() {
        TaskGraph graph = new TaskGraph();
        graph.add(TaskRecord.of("A", "调研", List.of()));
        graph.add(TaskRecord.of("B", "分析", List.of("A")));
        graph.add(TaskRecord.of("C", "报告", List.of("B")));

        // 初始：只有 A ready（B 等 A，C 等 B）
        assertThat(graph.readyTasks()).extracting(TaskRecord::id).containsExactly("A");
    }

    @Test
    void afterDone_dependencyUnblocksNext() {
        TaskGraph graph = new TaskGraph();
        graph.add(TaskRecord.of("A", "调研", List.of()));
        graph.add(TaskRecord.of("B", "分析", List.of("A")));

        TaskRecord a = graph.get("A").orElseThrow();
        graph.update(a.done("ok"));

        // A 完成后 B 才 ready
        assertThat(graph.readyTasks()).extracting(TaskRecord::id).containsExactly("B");
    }

    @Test
    void runner_executesInDependencyOrder() {
        TaskGraph graph = new TaskGraph();
        graph.add(TaskRecord.of("A", "调研", List.of()));
        graph.add(TaskRecord.of("B", "分析", List.of("A")));
        graph.add(TaskRecord.of("C", "报告", List.of("B")));

        TaskRunner runner = new TaskRunner(new TaskStore(
                java.nio.file.Path.of(".memory", "test-runner.json")), t -> "done");
        runner.run(graph);

        assertThat(graph.allSettled()).isTrue();
        assertThat(graph.get("A").orElseThrow().status()).isEqualTo(TaskStatus.DONE);
        assertThat(graph.get("B").orElseThrow().status()).isEqualTo(TaskStatus.DONE);
        assertThat(graph.get("C").orElseThrow().status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void persistence_saveAndLoad_restoresState() {
        TaskGraph graph = new TaskGraph();
        graph.add(TaskRecord.of("A", "调研", List.of()));
        graph.add(TaskRecord.of("B", "分析", List.of("A")));
        TaskStore store = new TaskStore(java.nio.file.Path.of(".memory", "test-persist.json"));

        // 完成 A，B 还在 PENDING
        graph.update(graph.get("A").orElseThrow().done("调研结果"));
        store.save(graph);

        // 重新加载
        TaskGraph loaded = store.load();
        assertThat(loaded.get("A").orElseThrow().status()).isEqualTo(TaskStatus.DONE);
        assertThat(loaded.get("B").orElseThrow().status()).isEqualTo(TaskStatus.PENDING);
        // B 现在 ready（因为 A 已 DONE）
        assertThat(loaded.readyTasks()).extracting(TaskRecord::id).containsExactly("B");
    }
}
