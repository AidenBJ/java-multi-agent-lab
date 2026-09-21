package com.example.agentlab.taskboard;

import java.util.List;

/**
 * 自组织协调器：没有领导派活，worker 自己看任务板认领匹配的任务。
  * @author guoxiangyue
 */
public class AutonomousCoordinator {

    private final TaskBoard board;
    private final List<Worker> workers;

    public AutonomousCoordinator(TaskBoard board, List<Worker> workers) {
        this.board = board;
        this.workers = List.copyOf(workers);
    }

    /**
     * 跑一轮：每个 worker 按技能找一个 PENDING 任务，乐观锁认领后执行并完成。
     * 返回本轮认领的任务数。
     */
    public int pollOnce(long now) {
        int claimed = 0;
        for (Worker w : workers) {
            List<TaskCard> candidates = board.listBySkill(w.skill());
            for (TaskCard task : candidates) {
                if (board.claim(task.id(), w.id(), now)) {
                    String result = w.handle(task);
                    board.complete(task.id(), w.id(), result);
                    claimed++;
                    break; // 一个 worker 一轮只干一个活
                }
            }
        }
        return claimed;
    }
}
