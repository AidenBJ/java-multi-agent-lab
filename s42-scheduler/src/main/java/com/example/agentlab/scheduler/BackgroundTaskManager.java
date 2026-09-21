package com.example.agentlab.scheduler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 后台任务管理器：慢操作丢后台虚拟线程，立即返回 taskId，完成后查结果。
 * 对齐 s13：后台执行不阻塞主循环，完成结果稍后注入。
  * @author guoxiangyue
 */
public class BackgroundTaskManager {

    /** 任务状态。 */
    public enum Status { RUNNING, COMPLETED, FAILED }

    private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
    private final ConcurrentHashMap<String, Future<String>> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Status> statuses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> results = new ConcurrentHashMap<>();
    private int counter = 0;

    /** 提交后台任务，立即返回 taskId。 */
    public synchronized String submit(java.util.concurrent.Callable<String> task) {
        String id = "bg-" + (++counter);
        statuses.put(id, Status.RUNNING);
        Future<String> future = pool.submit(() -> {
            try {
                String r = task.call();
                statuses.put(id, Status.COMPLETED);
                results.put(id, r);
                return r;
            } catch (Exception e) {
                statuses.put(id, Status.FAILED);
                results.put(id, "ERROR: " + e.getMessage());
                throw e;
            }
        });
        tasks.put(id, future);
        return id;
    }

    public Status status(String taskId) {
        return statuses.getOrDefault(taskId, Status.RUNNING);
    }

    public String result(String taskId) {
        return results.get(taskId);
    }

    public void shutdown() {
        pool.shutdown();
    }
}
