package com.example.agentlab.subagent;

/**
 * 一个子任务：由某个工人（workerId）执行的、自包含的指令。
 *
 * <p>关键：instruction 必须自包含——工人看不到其他任何上下文（上下文隔离）。</p>
  * @author guoxiangyue
 */
public record Subtask(String id, String workerId, String instruction) {
}
