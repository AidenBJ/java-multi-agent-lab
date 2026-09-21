package com.example.agentlab.checkpoint;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 检查点离线测试：落盘、崩溃恢复、时间旅行。
 */
class CheckpointedRunnerTest {

    static Step step(String name, java.util.function.BiConsumer<Map<String, Object>, String> c) {
        return new Step() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public Map<String, Object> execute(Map<String, Object> state) {
                c.accept(state, name);
                return state;
            }
        };
    }

    @Test
    void run_savesEveryStep() {
        CheckpointStore store = new MemoryCheckpointStore();
        CheckpointedRunner runner = new CheckpointedRunner(store);
        List<Step> steps = List.of(
                step("a", (s, n) -> s.put("a", "A")),
                step("b", (s, n) -> s.put("b", "B")));

        Map<String, Object> finalState = runner.run("t1", steps, Map.of("init", "0"));

        assertThat(finalState).containsEntry("a", "A").containsEntry("b", "B");
        // init(0) + a(1) + b(2) = 3 个检查点
        assertThat(store.listHistory("t1")).hasSize(3);
    }

    @Test
    void resume_fromLatestCheckpoint_completesRemainingSteps() {
        CheckpointStore store = new MemoryCheckpointStore();
        CheckpointedRunner runner = new CheckpointedRunner(store);
        List<Step> steps = List.of(
                step("a", (s, n) -> s.put("a", "A")),
                step("b", (s, n) -> s.put("b", "B")),
                step("c", (s, n) -> s.put("c", "C")));

        // 只跑前两步（模拟崩溃在 b 之后、c 之前）
        runner.run("t2", steps.subList(0, 2), Map.of("init", "0"));
        assertThat(store.loadLatest("t2").orElseThrow().step()).isEqualTo(2);

        // 恢复：继续 c
        Map<String, Object> finalState = runner.resume("t2", steps);

        assertThat(finalState).containsEntry("a", "A").containsEntry("b", "B").containsEntry("c", "C");
    }

    @Test
    void resume_withoutCheckpoint_throws() {
        CheckpointedRunner runner = new CheckpointedRunner(new MemoryCheckpointStore());
        assertThatThrownBy(() -> runner.resume("empty", List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void history_enablesTimeTravel() {
        CheckpointStore store = new MemoryCheckpointStore();
        CheckpointedRunner runner = new CheckpointedRunner(store);
        List<Step> steps = List.of(
                step("a", (s, n) -> s.put("a", "A")),
                step("b", (s, n) -> s.put("b", "B")));

        runner.run("t3", steps, Map.of("init", "0"));

        List<Checkpoint> history = store.listHistory("t3");
        assertThat(history).hasSize(3);
        // step1 只有 a，step2 有 a+b（时间旅行能看到中间状态）
        assertThat(history.get(1).state()).containsEntry("a", "A").doesNotContainKey("b");
        assertThat(history.get(2).state()).containsEntry("b", "B");
    }
}
