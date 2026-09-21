package com.example.agentlab.scheduler;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 后台任务与定时调度离线测试。
 */
class SchedulerTest {

    @Test
    void backgroundTask_submitReturnsImmediately() throws Exception {
        BackgroundTaskManager bg = new BackgroundTaskManager();
        String id = bg.submit(() -> {
            Thread.sleep(200);
            return "done";
        });
        // 立即返回，任务还在跑
        assertThat(bg.status(id)).isEqualTo(BackgroundTaskManager.Status.RUNNING);
        bg.shutdown();
    }

    @Test
    void backgroundTask_resultAvailableLater() throws Exception {
        BackgroundTaskManager bg = new BackgroundTaskManager();
        String id = bg.submit(() -> {
            Thread.sleep(100);
            return "慢活完成";
        });
        Thread.sleep(300);
        assertThat(bg.status(id)).isEqualTo(BackgroundTaskManager.Status.COMPLETED);
        assertThat(bg.result(id)).isEqualTo("慢活完成");
        bg.shutdown();
    }

    @Test
    void cronScheduler_triggersAtIntervals() throws Exception {
        CronScheduler cron = new CronScheduler();
        cron.scheduleAtFixedRate(() -> {}, 50, 100);
        Thread.sleep(400);
        cron.shutdown();
        // 400ms 内至少触发 2 次（initialDelay 50 + period 100）
        assertThat(cron.logs().size()).isGreaterThanOrEqualTo(2);
    }
}
