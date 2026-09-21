package com.example.agentlab.orchestrator;

import java.util.List;

/**
 * S3.2 离线 Demo：桩 Orchestrator-Worker，对比"串行 vs 并行"耗时。
 *
 * <pre>
 *   mvn -q -pl orchestrator exec:java "-Dexec.mainClass=com.example.agentlab.orchestrator.OrchestratorDemo"
 * </pre>
  * @author guoxiangyue
 */
public class OrchestratorDemo {

    static final long WORKER_SLEEP_MS = 300;

    public static void main(String[] args) throws Exception {
        System.out.println("=== S3.2 Orchestrator-Worker Demo（离线）===");

        TaskSplitter splitter = task -> List.of(
                "调研市场规模", "调研竞品功能", "调研用户画像");
        List<ParallelWorker> workers = List.of(
                stubWorker("researcher-a"),
                stubWorker("researcher-b"),
                stubWorker("researcher-c"));
        Aggregator aggregator = (task, results) ->
                "《" + task + "》最终报告：\n" + String.join("\n", results);

        OrchestratorGraph graph = new OrchestratorGraph(splitter, workers, aggregator);

        long start = System.currentTimeMillis();
        String report = graph.run("行业调研报告");
        long elapsed = System.currentTimeMillis() - start;

        System.out.println(report);
        System.out.println("----");
        System.out.println("图执行耗时: " + elapsed + "ms"
                + "（串行 3×" + WORKER_SLEEP_MS + "ms ≈ " + (3 * WORKER_SLEEP_MS) + "ms）");
    }

    static ParallelWorker stubWorker(String id) {
        return new ParallelWorker() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String handle(String subtask, String task) {
                try {
                    Thread.sleep(WORKER_SLEEP_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return id + " 完成: " + subtask + "（耗时 " + WORKER_SLEEP_MS + "ms，线程 "
                        + Thread.currentThread().getName() + "）";
            }
        };
    }
}
