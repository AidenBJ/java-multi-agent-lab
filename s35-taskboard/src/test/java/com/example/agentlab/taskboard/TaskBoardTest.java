package com.example.agentlab.taskboard;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 任务板离线测试：乐观锁不重复认领、技能匹配、宕机交接。
 */
class TaskBoardTest {

    static Worker stub(String id, String skill) {
        return new Worker() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String skill() {
                return skill;
            }

            @Override
            public String handle(TaskCard task) {
                return id + " done";
            }
        };
    }

    @Test
    void claim_onlyOnce_noDuplicate() {
        TaskBoard board = new TaskBoard();
        board.submit(TaskCard.pending("t1", "活", "coding"));

        boolean first = board.claim("t1", "coder-a", 1000);
        boolean second = board.claim("t1", "coder-b", 1000);

        assertThat(first).isTrue();
        assertThat(second).isFalse(); // 乐观锁：第二个抢不到
    }

    @Test
    void workerClaimsSkillMatchedTask() {
        TaskBoard board = new TaskBoard();
        board.submit(TaskCard.pending("t1", "写周报", "writing"));
        board.submit(TaskCard.pending("t2", "写代码", "coding"));

        AutonomousCoordinator coordinator = new AutonomousCoordinator(board,
                List.of(stub("writer", "writing"), stub("coder", "coding")));

        coordinator.pollOnce(1000);

        TaskCard t1 = board.get("t1").orElseThrow();
        TaskCard t2 = board.get("t2").orElseThrow();
        assertThat(t1.status()).isEqualTo(TaskStatus.DONE);
        assertThat(t1.claimedBy()).isEqualTo("writer");
        assertThat(t2.claimedBy()).isEqualTo("coder");
    }

    @Test
    void crashedWorker_timeoutReleased_othersCanTakeOver() {
        TaskBoard board = new TaskBoard();
        board.submit(TaskCard.pending("t1", "写代码", "coding"));

        // coder-a 认领了但宕机（没 complete）
        board.claim("t1", "coder-a", 1000);
        assertThat(board.get("t1").orElseThrow().claimedBy()).isEqualTo("coder-a");

        // 超时释放：认领时间 1000，现在 1000+60000，超时 5000ms → 释放
        int released = board.releaseTimeoutClaimed(5000, 7000);
        assertThat(released).isEqualTo(1);
        assertThat(board.get("t1").orElseThrow().status()).isEqualTo(TaskStatus.PENDING);

        // coder-b 接手
        AutonomousCoordinator coordinator = new AutonomousCoordinator(board,
                List.of(stub("coder-b", "coding")));
        coordinator.pollOnce(8000);

        TaskCard t1 = board.get("t1").orElseThrow();
        assertThat(t1.claimedBy()).isEqualTo("coder-b");
        assertThat(t1.status()).isEqualTo(TaskStatus.DONE);
    }
}
