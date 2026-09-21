package com.example.agentlab.subagent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 任务派发器（纯逻辑，可离线测试）：按计划把每个子任务派给对应工人，收集结论。
 *
 * <p>工人通过函数注入：真实场景传 {@code workerPool::get}，测试传桩。每个子任务的结果
 * 只保留结论（output），中间过程不回流——这是"只回传结论"的实现点。</p>
  * @author guoxiangyue
 */
public class TaskDispatcher {

    /** 派发整个计划，返回每个子任务的结果（按计划顺序）。 */
    public List<SubtaskResult> dispatch(SubtaskPlan plan, Function<String, WorkerAgent> workerProvider) {
        List<SubtaskResult> results = new ArrayList<>();
        for (Subtask subtask : plan.subtasks()) {
            WorkerAgent worker = workerProvider.apply(subtask.workerId());
            if (worker == null) {
                results.add(new SubtaskResult(subtask.id(), subtask.workerId(),
                        "错误：未找到工人 " + subtask.workerId()));
                continue;
            }
            String output = worker.execute(subtask.instruction());
            results.add(new SubtaskResult(subtask.id(), subtask.workerId(), output));
        }
        return results;
    }
}
