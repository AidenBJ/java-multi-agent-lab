package com.example.agentlab.taskboard;

/**
 * 任务板上的一张任务卡。
 *
 * @param id           任务 ID
 * @param title        标题
 * @param requiredSkill 需要的技能（worker 按技能匹配认领）
 * @param status       状态
 * @param claimedBy    认领者（null=未认领）
 * @param result       完成结果（null=未完成）
 * @param claimedAt    认领时间戳（用于超时释放）
  * @author guoxiangyue
 */
public record TaskCard(
        String id,
        String title,
        String requiredSkill,
        TaskStatus status,
        String claimedBy,
        String result,
        long claimedAt) {

    public static TaskCard pending(String id, String title, String requiredSkill) {
        return new TaskCard(id, title, requiredSkill, TaskStatus.PENDING, null, null, 0);
    }

    public TaskCard claim(String workerId, long now) {
        return new TaskCard(id, title, requiredSkill, TaskStatus.CLAIMED, workerId, result, now);
    }

    public TaskCard complete(String result) {
        return new TaskCard(id, title, requiredSkill, TaskStatus.DONE, claimedBy, result, claimedAt);
    }

    public TaskCard release() {
        return new TaskCard(id, title, requiredSkill, TaskStatus.PENDING, null, null, 0);
    }
}
