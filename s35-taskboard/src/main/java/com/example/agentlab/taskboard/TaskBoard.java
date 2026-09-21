package com.example.agentlab.taskboard;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 共享任务板。认领用乐观锁：同一任务只有一个 worker 能从 PENDING 变 CLAIMED。
 *
 * @author guoxiangyue
 */
public class TaskBoard {

    /** 任务存储：id → TaskCard */
    private final Map<String, TaskCard> cards = new ConcurrentHashMap<>();

    public void submit(TaskCard card) {
        cards.put(card.id(), card);
    }

    public Optional<TaskCard> get(String id) {
        return Optional.ofNullable(cards.get(id));
    }

    /** 乐观锁认领：只有 PENDING 能被认领，返回是否成功。 */
    public synchronized boolean claim(String taskId, String workerId, long now) {
        TaskCard card = cards.get(taskId);
        if (card == null || card.status() != TaskStatus.PENDING) {
            return false;
        }
        cards.put(taskId, card.claim(workerId, now));
        return true;
    }

    public synchronized void complete(String taskId, String workerId, String result) {
        TaskCard card = cards.get(taskId);
        if (card != null && card.status() == TaskStatus.CLAIMED && workerId.equals(card.claimedBy())) {
            cards.put(taskId, card.complete(result));
        }
    }

    /** 列出所有 PENDING 任务。 */
    public List<TaskCard> listPending() {
        return cards.values().stream()
                .filter(c -> c.status() == TaskStatus.PENDING)
                .collect(Collectors.toList());
    }

    /** 列出某技能可认领的 PENDING 任务。 */
    public List<TaskCard> listBySkill(String skill) {
        return listPending().stream()
                .filter(c -> c.requiredSkill().equals(skill))
                .collect(Collectors.toList());
    }

    /** 超时释放：认领超过 timeoutMs 仍未完成的，释放回 PENDING（宕机交接）。返回释放的任务数。 */
    public synchronized int releaseTimeoutClaimed(long timeoutMs, long now) {
        int released = 0;
        for (TaskCard card : cards.values()) {
            if (card.status() == TaskStatus.CLAIMED && now - card.claimedAt() > timeoutMs) {
                cards.put(card.id(), card.release());
                released++;
            }
        }
        return released;
    }
}
