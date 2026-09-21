package com.example.agentlab.subagent;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实调用 DeepSeek：Supervisor 完整链路（拆解 → 隔离工人 → 聚合）。
 * 未设置 DEEPSEEK_API_KEY 时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class SupervisorIT {

    @Test
    void supervisor_splitsExecutesAndAggregates() {
        ChatModel model = ModelFactory.createDefaultChatModel();
        WorkerPool pool = WorkerPool.createStandard(model);
        SubtaskPlanner planner = AiServices.builder(SubtaskPlanner.class).chatModel(model).build();
        ResultAggregator aggregator = AiServices.builder(ResultAggregator.class).chatModel(model).build();
        Supervisor supervisor = new Supervisor(planner, new TaskDispatcher(), aggregator, pool);

        Report report = supervisor.run("对'Java 多 Agent 协同'做一个 2 步的简短行业观察");

        assertThat(report.subtasks()).isNotEmpty();
        assertThat(report.results()).hasSize(report.subtasks().size());
        assertThat(report.finalReport()).isNotBlank();
        System.out.println("计划: " + report.subtasks().size() + " 个子任务");
        System.out.println("最终报告片段: " + report.finalReport().substring(0, Math.min(100, report.finalReport().length())));
    }

    @Test
    void taskTool_spawnsSubagent_andReturnsOnlyConclusion() {
        ChatModel model = ModelFactory.createDefaultChatModel();
        SubagentRunner runner = new SubagentRunner(model);

        String conclusion = runner.spawn("请用一句话回答：什么是上下文隔离？");

        assertThat(conclusion).isNotBlank();
        System.out.println("子 Agent 结论: " + conclusion);
    }
}
