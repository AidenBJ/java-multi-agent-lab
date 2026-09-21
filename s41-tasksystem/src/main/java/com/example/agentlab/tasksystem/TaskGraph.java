package com.example.agentlab.tasksystem;

import java.util.*;

/**
 * 任务图：管理任务间 DAG 依赖，找出"就绪"（blockedBy 全 DONE）的任务。
 *
 * @author guoxiangyue
 */
public class TaskGraph {

    /** 任务存储：id → TaskRecord */
    private final Map<String, TaskRecord> tasks = new LinkedHashMap<>();

    public void add(TaskRecord task) {
        tasks.put(task.id(), task);
    }

    public Optional<TaskRecord> get(String id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Collection<TaskRecord> all() {
        return Collections.unmodifiableCollection(tasks.values());
    }

    /** 找出所有就绪任务：PENDING 且 blockedBy 全部 DONE。 */
    public List<TaskRecord> readyTasks() {
        List<TaskRecord> ready = new ArrayList<>();
        for (TaskRecord t : tasks.values()) {
            if (t.status() != TaskStatus.PENDING) {
                continue;
            }
            boolean blocked = false;
            for (String depId : t.blockedBy()) {
                TaskRecord dep = tasks.get(depId);
                if (dep == null || dep.status() != TaskStatus.DONE) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                ready.add(t);
            }
        }
        return ready;
    }

    public void update(TaskRecord task) {
        tasks.put(task.id(), task);
    }

    /** 是否全部完成（DONE 或 FAILED）。 */
    public boolean allSettled() {
        return tasks.values().stream()
                .allMatch(t -> t.status() == TaskStatus.DONE || t.status() == TaskStatus.FAILED);
    }

    /** 任务图可视化（文本）。 */
    public String visualize() {
        StringBuilder sb = new StringBuilder("任务图:\n");
        for (TaskRecord t : tasks.values()) {
            sb.append("  ").append(t.id()).append(" [").append(t.status()).append("] ")
                    .append(t.title());
            if (!t.blockedBy().isEmpty()) {
                sb.append("  (依赖: ").append(String.join(",", t.blockedBy())).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
