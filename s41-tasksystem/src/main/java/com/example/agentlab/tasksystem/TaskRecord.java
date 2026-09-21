package com.example.agentlab.tasksystem;

import java.util.List;

/**
 * 一条任务记录。
 *
 * @param id         任务 ID
 * @param title      标题
 * @param blockedBy  依赖的任务 ID 列表（全部 DONE 后本任务才可执行）
 * @param status     状态
 * @param result     完成结果（null=未完成）
  * @author guoxiangyue
 */
public record TaskRecord(
        String id,
        String title,
        List<String> blockedBy,
        TaskStatus status,
        String result) {

    public static TaskRecord of(String id, String title, List<String> blockedBy) {
        return new TaskRecord(id, title, List.copyOf(blockedBy), TaskStatus.PENDING, null);
    }

    public TaskRecord running() {
        return new TaskRecord(id, title, blockedBy, TaskStatus.RUNNING, result);
    }

    public TaskRecord done(String result) {
        return new TaskRecord(id, title, blockedBy, TaskStatus.DONE, result);
    }

    public TaskRecord failed(String reason) {
        return new TaskRecord(id, title, blockedBy, TaskStatus.FAILED, reason);
    }
}
