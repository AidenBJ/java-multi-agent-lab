package com.example.agentlab.scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 简易定时调度器：用 ScheduledExecutorService 到点自动触发任务。
 * 对齐 s14：到点自动执行，不需要人推。
  * @author guoxiangyue
 */
public class CronScheduler {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentLinkedQueue<String> logs = new ConcurrentLinkedQueue<>();

    /** 按固定间隔（毫秒）定时执行。 */
    public void scheduleAtFixedRate(Runnable task, long initialDelayMs, long periodMs) {
        executor.scheduleAtFixedRate(() -> {
            String log = "[" + System.currentTimeMillis() + "] 定时触发执行";
            logs.add(log);
            task.run();
        }, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
    }

    public ConcurrentLinkedQueue<String> logs() {
        return logs;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
