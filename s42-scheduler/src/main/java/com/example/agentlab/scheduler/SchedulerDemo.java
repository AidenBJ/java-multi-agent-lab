package com.example.agentlab.scheduler;

/**
 * S4.2 离线 Demo：后台任务不阻塞 + 定时调度。
 *
 * <pre>
 *   mvn -q -pl scheduler exec:java "-Dexec.mainClass=com.example.agentlab.scheduler.SchedulerDemo"
 * </pre>
  * @author guoxiangyue
 */
public class SchedulerDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== S4.2 后台任务与定时调度 Demo（离线）===");

        // 1. 后台任务：慢操作丢后台，立即返回
        BackgroundTaskManager bg = new BackgroundTaskManager();
        String taskId = bg.submit(() -> {
            Thread.sleep(500); // 模拟慢操作
            return "后台任务完成";
        });
        System.out.println("提交后台任务，立即返回: " + taskId + "（状态: " + bg.status(taskId) + "）");
        System.out.println("主线程继续干别的...");
        Thread.sleep(800); // 等它完成
        System.out.println("稍后查结果: " + bg.result(taskId));
        bg.shutdown();

        // 2. 定时调度：每 200ms 触发一次
        CronScheduler cron = new CronScheduler();
        cron.scheduleAtFixedRate(() -> System.out.println("  定时任务触发"), 100, 200);
        Thread.sleep(700);
        cron.shutdown();
        System.out.println("定时任务共触发 " + cron.logs().size() + " 次");
    }
}
