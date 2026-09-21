package com.example.agentlab.subagent;

import java.util.List;

/**
 * Supervisor 一次运行的完整产物：计划 → 子结果 → 最终报告。
  * @author guoxiangyue
 */
public record Report(String goal, List<Subtask> subtasks, List<SubtaskResult> results, String finalReport) {

    /** 把子结果拼成汇总员可读的文本。 */
    public String formatRawResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("总目标: ").append(goal).append("\n\n");
        for (SubtaskResult r : results) {
            sb.append("=== 子任务 ").append(r.subtaskId())
                    .append("（工人 ").append(r.workerId()).append("）===\n")
                    .append(r.output()).append("\n\n");
        }
        return sb.toString();
    }
}
