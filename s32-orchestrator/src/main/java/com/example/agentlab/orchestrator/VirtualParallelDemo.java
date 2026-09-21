package com.example.agentlab.orchestrator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * S3.2 补充：langgraph4j 1.8.27 的 fan-out 是<b>串行遍历分支</b>（实测 ≈ 串行耗时）。
 * 真并行下沉到节点内部——用 Java 21 虚拟线程并行跑各子任务，再 fan-in 聚合。
 *
 * <pre>
 *   mvn -q -pl orchestrator exec:java "-Dexec.mainClass=com.example.agentlab.orchestrator.VirtualParallelDemo"
 * </pre>
  * @author guoxiangyue
 */
public class VirtualParallelDemo {

    static final long WORKER_SLEEP_MS = 300;

    public static void main(String[] args) throws Exception {
        System.out.println("=== S3.2 补充：虚拟线程真并行（节点内部 fan-out）===");

        List<String> subtasks = List.of("调研市场规模", "调研竞品功能", "调研用户画像");

        long start = System.currentTimeMillis();
        List<String> results = parallelRun(subtasks);
        long elapsed = System.currentTimeMillis() - start;

        results.forEach(r -> System.out.println("  - " + r));
        System.out.println("----");
        System.out.println("虚拟线程并行耗时: " + elapsed + "ms"
                + "（串行 3×" + WORKER_SLEEP_MS + "ms ≈ " + (3 * WORKER_SLEEP_MS) + "ms）");
    }

    /** 用虚拟线程并行跑各子任务（fan-out），等全部完成（fan-in）。 */
    static List<String> parallelRun(List<String> subtasks) throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = subtasks.stream()
                    .map(subtask -> pool.submit(() -> {
                        Thread.sleep(WORKER_SLEEP_MS);
                        return "完成: " + subtask + "（线程 " + Thread.currentThread().getName() + "）";
                    }))
                    .collect(Collectors.toList());
            return futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
        }
    }
}
