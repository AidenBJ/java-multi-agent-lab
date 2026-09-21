package com.example.agentlab.subagent;

import java.util.List;

/**
 * Supervisor（主管）：拆解 → 派发 → 聚合。
 *
 * <p>编排本身不依赖 LLM 之外的东西，三个环节各自可替换（测试注入桩）。</p>
  * @author guoxiangyue
 */
public class Supervisor {

    private final SubtaskPlanner planner;
    private final TaskDispatcher dispatcher;
    private final ResultAggregator aggregator;
    private final WorkerPool workerPool;

    public Supervisor(SubtaskPlanner planner, TaskDispatcher dispatcher,
                      ResultAggregator aggregator, WorkerPool workerPool) {
        this.planner = planner;
        this.dispatcher = dispatcher;
        this.aggregator = aggregator;
        this.workerPool = workerPool;
    }

    /** 跑一轮完整的"拆解 → 派发 → 聚合"。 */
    public Report run(String task) {
        SubtaskPlan plan = planner.split(task);
        List<SubtaskResult> results = dispatcher.dispatch(plan, workerPool::get);
        String finalReport = aggregator.aggregate(new Report(plan.goal(), plan.subtasks(), results, "").formatRawResults());
        return new Report(plan.goal(), plan.subtasks(), results, finalReport);
    }
}
