package com.example.agentlab.taskboard;

/**
 * 任务状态。
  * @author guoxiangyue
 */
public enum TaskStatus {
    /** 待认领。 */
    PENDING,
    /** 已认领（执行中）。 */
    CLAIMED,
    /** 已完成。 */
    DONE
}
