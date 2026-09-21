package com.example.agentlab.orchestrator;

import java.util.List;

/**
 * 任务拆解器：把大任务拆成 N 个可并行的子任务。
  * @author guoxiangyue
 */
public interface TaskSplitter {

    List<String> split(String task);
}
