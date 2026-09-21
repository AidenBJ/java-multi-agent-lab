package com.example.agentlab.taskboard;

import java.util.List;

/**
 * S3.5 离线 Demo：3 个 Worker 共看任务板，自己认领匹配的任务。
 *
 * <pre>
 *   mvn -q -pl taskboard exec:java "-Dexec.mainClass=com.example.agentlab.taskboard.TaskBoardDemo"
 * </pre>
  * @author guoxiangyue
 */
public class TaskBoardDemo {

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
                return id + " 完成: " + task.title();
            }
        };
    }

    public static void main(String[] args) {
        System.out.println("=== S3.5 自组织任务板 Demo（离线）===");
        TaskBoard board = new TaskBoard();
        board.submit(TaskCard.pending("t1", "写周报", "writing"));
        board.submit(TaskCard.pending("t2", "写 Java 接口", "coding"));
        board.submit(TaskCard.pending("t3", "做数据看板", "analysis"));

        AutonomousCoordinator coordinator = new AutonomousCoordinator(board,
                List.of(stub("writer", "writing"), stub("coder", "coding"), stub("analyst", "analysis")));

        int claimed = coordinator.pollOnce(System.currentTimeMillis());
        System.out.println("本轮认领任务数: " + claimed);

        board.listPending().forEach(t -> System.out.println("  待认领: " + t.title()));
        System.out.println("（全部 PENDING 都被匹配的 worker 认领完成，上面应为空）");
    }
}
