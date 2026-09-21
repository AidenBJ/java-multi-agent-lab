package com.example.agentlab.orchestrator;

import java.util.List;

/**
 * 并行 worker：处理一个子任务，返回结果。
  * @author guoxiangyue
 */
public interface ParallelWorker {

    String id();

    String handle(String subtask, String task);
}
