package com.example.agentlab.tasksystem;

import java.util.function.Function;

/**
 * 任务执行器：循环找就绪任务 → 执行 → 标记 → 落盘。
  * @author guoxiangyue
 */
public class TaskRunner {

    private final TaskStore store;
    private final Function<TaskRecord, String> executor;

    public TaskRunner(TaskStore store, Function<TaskRecord, String> executor) {
        this.store = store;
        this.executor = executor;
    }

    /**
     * 跑任务图：反复找就绪任务执行，直到全部 settled。
     * 每步落盘——崩溃后可从 load 续跑。
     */
    public void run(TaskGraph graph) {
        while (!graph.allSettled()) {
            var ready = graph.readyTasks();
            if (ready.isEmpty()) {
                break; // 死锁：还有 PENDING 但都被未完成依赖卡住
            }
            for (TaskRecord task : ready) {
                TaskRecord running = task.running();
                graph.update(running);
                store.save(graph);
                try {
                    String result = executor.apply(task);
                    graph.update(running.done(result));
                } catch (Exception e) {
                    graph.update(running.failed(e.getMessage()));
                }
                store.save(graph);
            }
        }
    }
}
