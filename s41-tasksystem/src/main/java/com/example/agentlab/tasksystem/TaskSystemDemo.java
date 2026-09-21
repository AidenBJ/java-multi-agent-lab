package com.example.agentlab.tasksystem;

import java.nio.file.Path;
import java.util.List;

/**
 * S4.1 离线 Demo：任务图 DAG 执行 + 磁盘持久化 + 崩溃续跑。
 *
 * <pre>
 *   mvn -q -pl tasksystem exec:java "-Dexec.mainClass=com.example.agentlab.tasksystem.TaskSystemDemo"
 * </pre>
  * @author guoxiangyue
 */
public class TaskSystemDemo {

    public static void main(String[] args) {
        System.out.println("=== S4.1 持久化任务系统 Demo（离线）===");
        Path file = Path.of(".memory", "task-demo.json");
        TaskStore store = new TaskStore(file);

        // A → B → C 依赖链
        TaskGraph graph = new TaskGraph();
        graph.add(TaskRecord.of("A", "调研", List.of()));
        graph.add(TaskRecord.of("B", "分析", List.of("A")));
        graph.add(TaskRecord.of("C", "写报告", List.of("B")));

        TaskRunner runner = new TaskRunner(store, task -> "[" + task.id() + "]完成");
        runner.run(graph);

        System.out.println(graph.visualize());

        // 模拟重启：从磁盘加载，续跑（此时已全 DONE，直接结束）
        TaskGraph loaded = store.load();
        System.out.println("从磁盘加载后状态:");
        for (var t : loaded.all()) {
            System.out.println("  " + t.id() + " = " + t.status() + " " + t.result());
        }
    }
}
