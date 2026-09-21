package com.example.agentlab.subagent;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * S2.2 Demo B（ROADMAP 设计）：Supervisor 拆解 → 3 个隔离 Worker 并行干活 → 汇总报告。
 *
 * <pre>
 *   mvn -q -pl subagent exec:java "-Dexec.mainClass=com.example.agentlab.subagent.SupervisorApp"
 * </pre>
  * @author guoxiangyue
 */
public class SupervisorApp {

    public static void main(String[] args) {
        ChatModel model = ModelFactory.createDefaultChatModel();

        WorkerPool pool = WorkerPool.createStandard(model);
        SubtaskPlanner planner = AiServices.builder(SubtaskPlanner.class).chatModel(model).build();
        ResultAggregator aggregator = AiServices.builder(ResultAggregator.class).chatModel(model).build();
        Supervisor supervisor = new Supervisor(planner, new TaskDispatcher(), aggregator, pool);

        String task = args.length > 0
                ? String.join(" ", args)
                : "对'Java 多 Agent 协同'做一个简短的行业观察（研究趋势、评估技术栈、写一段总结）";

        System.out.println("总任务: " + task);
        System.out.println("工人池: " + pool.workerIds());
        System.out.println();

        Report report = supervisor.run(task);

        System.out.println("=== 拆解计划（" + report.subtasks().size() + " 个子任务）===");
        for (Subtask s : report.subtasks()) {
            System.out.println("  [" + s.workerId() + "] " + s.id() + ": " + s.instruction());
        }
        System.out.println();
        System.out.println("=== 各子任务结果（只回传结论）===");
        for (SubtaskResult r : report.results()) {
            System.out.println("--- " + r.workerId() + " / " + r.subtaskId() + " ---");
            System.out.println(r.output());
            System.out.println();
        }
        System.out.println("=== 最终报告（汇总）===");
        System.out.println(report.finalReport());
    }
}
