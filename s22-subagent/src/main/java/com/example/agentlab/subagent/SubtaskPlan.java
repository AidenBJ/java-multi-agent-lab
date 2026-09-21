package com.example.agentlab.subagent;

import java.util.List;

/**
 * 拆解后的子任务计划：总目标 + 子任务清单（AiServices 结构化返回）。
  * @author guoxiangyue
 */
public record SubtaskPlan(String goal, List<Subtask> subtasks) {
}
