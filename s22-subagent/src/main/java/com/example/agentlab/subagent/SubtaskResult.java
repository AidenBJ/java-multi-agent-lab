package com.example.agentlab.subagent;

/**
 * 一个子任务的执行结果：只有结论（output），不带中间过程。
  * @author guoxiangyue
 */
public record SubtaskResult(String subtaskId, String workerId, String output) {
}
