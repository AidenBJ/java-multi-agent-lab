package com.example.agentlab.checkpoint;

import java.util.List;
import java.util.Map;

/**
 * S3.4 离线 Demo：流水线跑到一半"崩溃"，重启从检查点恢复继续；时间旅行回看每步状态。
 *
 * <pre>
 *   mvn -q -pl checkpoint exec:java "-Dexec.mainClass=com.example.agentlab.checkpoint.CheckpointDemo"
 * </pre>
  * @author guoxiangyue
 */
public class CheckpointDemo {

    public static void main(String[] args) {
        System.out.println("=== S3.4 检查点 Demo（离线）===");
        CheckpointStore store = new MemoryCheckpointStore();
        CheckpointedRunner runner = new CheckpointedRunner(store);
        String threadId = "report-task-1";

        List<Step> steps = List.of(
                step("research", s -> {
                    s.put("research", "市场规模 100 亿");
                    return s;
                }),
                step("analyze", s -> {
                    s.put("analysis", "年增 20%");
                    return s;
                }),
                step("write", s -> {
                    s.put("report", "《行业报告》");
                    return s;
                }));

        // 第一次：跑到 analyze 后"崩溃"
        System.out.println("---- 第一次执行（跑到 analyze 后崩溃）----");
        try {
            Map<String, Object> state = runner.run(threadId, steps, Map.of("topic", "行业调研"));
            System.out.println("完整跑完: " + state);
        } catch (RuntimeException e) {
            System.out.println("崩溃: " + e.getMessage());
        }
        // 手动制造崩溃：只跑前两步（模拟 analyze 之后、write 之前进程挂了）
        runner.run(threadId + "_crashed", steps.subList(0, 2), Map.of("topic", "行业调研"));

        // 重启：从检查点恢复，继续 write
        System.out.println("---- 重启：从检查点恢复，继续 write ----");
        Map<String, Object> finalState = runner.resume(threadId + "_crashed", steps);
        System.out.println("恢复后最终状态: " + finalState);

        // 时间旅行：回看每一步
        System.out.println("---- 时间旅行：每一步状态 ----");
        store.listHistory(threadId + "_crashed").forEach(cp ->
                System.out.println("  step " + cp.step() + " [" + cp.node() + "] → " + cp.state()));
    }

    static Step step(String name, java.util.function.Function<Map<String, Object>, Map<String, Object>> fn) {
        return new Step() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Map<String, Object> execute(Map<String, Object> state) {
                return fn.apply(state);
            }
        };
    }
}
