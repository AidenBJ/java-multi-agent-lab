package com.example.agentlab.tasksystem;

/**
 * 任务状态机。
  * @author guoxiangyue
 */
public enum TaskStatus {
    /** 待执行。 */
    PENDING,
    /** 执行中。 */
    RUNNING,
    /** 已完成。 */
    DONE,
    /** 失败。 */
    FAILED
}
