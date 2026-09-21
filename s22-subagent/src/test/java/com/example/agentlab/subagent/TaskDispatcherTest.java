package com.example.agentlab.subagent;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 任务派发器离线测试：用桩工人验证派发顺序、未知工人容错。
 */
class TaskDispatcherTest {

    @Test
    void dispatch_runsEverySubtask_inOrder() {
        SubtaskPlan plan = new SubtaskPlan("调研",
                List.of(
                        new Subtask("t1", "research", "研究趋势"),
                        new Subtask("t2", "coding", "评估技术栈"),
                        new Subtask("t3", "writing", "写总结")
                ));

        // 桩工人：直接回显指令（模拟"只回传结论"）
        TaskDispatcher dispatcher = new TaskDispatcher();
        List<SubtaskResult> results = dispatcher.dispatch(plan, workerId ->
                (WorkerAgent) task -> "[" + workerId + "] 完成: " + task);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).workerId()).isEqualTo("research");
        assertThat(results.get(0).output()).isEqualTo("[research] 完成: 研究趋势");
        assertThat(results.get(2).workerId()).isEqualTo("writing");
    }

    @Test
    void dispatch_unknownWorker_returnsErrorResult() {
        SubtaskPlan plan = new SubtaskPlan("任务",
                List.of(new Subtask("t1", "ghost", "做点什么")));

        TaskDispatcher dispatcher = new TaskDispatcher();
        List<SubtaskResult> results = dispatcher.dispatch(plan, workerId -> null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).output()).contains("未找到工人 ghost");
    }

    @Test
    void report_formatRawResults_includesGoalAndAllResults() {
        List<SubtaskResult> results = List.of(
                new SubtaskResult("t1", "research", "结论1")
        );
        Report report = new Report("目标", List.of(), results, "");

        String raw = report.formatRawResults();

        assertThat(raw)
                .contains("总目标: 目标")
                .contains("工人 research")
                .contains("结论1");
    }
}
