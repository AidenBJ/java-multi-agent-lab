package com.example.agentlab.checkpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 带检查点的流水线执行器：每步执行后落检查点；崩溃后可从最新检查点恢复继续。
 *
 * @author guoxiangyue
 */
public class CheckpointedRunner {

    /** 检查点存储 */
    private final CheckpointStore store;

    /**
     * 构造执行器。
     *
     * @param store 检查点存储
     */
    public CheckpointedRunner(CheckpointStore store) {
        this.store = store;
    }

    /**
     * 从头跑：从 initialState 开始，落 step=0 初始检查点，再依次跑各步。
     *
     * @param threadId    线程 ID
     * @param steps       步骤列表
     * @param initialState 初始状态
     * @return 最终状态
     */
    public Map<String, Object> run(String threadId, List<Step> steps, Map<String, Object> initialState) {
        Map<String, Object> state = new HashMap<>(initialState);
        // 落 step=0 初始检查点
        saveCheckpoint(threadId, 0, "init", state);
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            state = step.execute(state);
            // 跑完第 i 步后落 step=i+1
            saveCheckpoint(threadId, i + 1, step.name(), state);
        }
        return state;
    }

    /**
     * 从最新检查点恢复：从检查点 step 的下一步继续跑剩余步骤。
     *
     * @param threadId 线程 ID
     * @param steps    步骤列表
     * @return 最终状态
     */
    public Map<String, Object> resume(String threadId, List<Step> steps) {
        Optional<Checkpoint> latest = store.loadLatest(threadId);
        if (latest.isEmpty()) {
            throw new IllegalStateException("没有可恢复的检查点: " + threadId);
        }
        Checkpoint cp = latest.get();
        Map<String, Object> state = new HashMap<>(cp.state());
        // 已执行到 cp.step（0=初始），下一步是 steps.get(cp.step)
        for (int i = cp.step(); i < steps.size(); i++) {
            Step step = steps.get(i);
            state = step.execute(state);
            saveCheckpoint(threadId, i + 1, step.name(), state);
        }
        return state;
    }

    /**
     * 落一个检查点。
     */
    private void saveCheckpoint(String threadId, int step, String node, Map<String, Object> state) {
        store.save(threadId, new Checkpoint(step, node, new HashMap<>(state), System.currentTimeMillis()));
    }
}
