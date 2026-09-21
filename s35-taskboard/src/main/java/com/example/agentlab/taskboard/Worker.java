package com.example.agentlab.taskboard;

/**
 * 自主 Worker：按自己的技能找任务板上的活。
  * @author guoxiangyue
 */
public interface Worker {

    String id();

    /** 该 worker 掌握的技能。 */
    String skill();

    /** 处理任务，返回结果。 */
    String handle(TaskCard task);
}
