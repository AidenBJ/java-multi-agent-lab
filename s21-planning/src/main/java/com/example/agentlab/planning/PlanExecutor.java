package com.example.agentlab.planning;

import java.util.List;
import java.util.function.Consumer;

/**
 * 计划执行器：执行-校验闭环。
 *
 * <p>逐项执行计划：每步由调用方注入的 {@link StepRunner} 真正执行，
 * 执行完成后勾选 completed 并同步回共享清单。真实执行（调用工具/子 Agent）在 S2.2 引入。</p>
  * @author guoxiangyue
 */
public class PlanExecutor {

    /** 单步执行器：真正执行"这一步做什么"。 */
    @FunctionalInterface
    public interface StepRunner {
        void run(TodoItem item) throws Exception;
    }

    private final TodoList todoList;

    public PlanExecutor(TodoList todoList) {
        this.todoList = todoList;
    }

    /** 执行整个计划，返回全部完成后的条目。 */
    public List<TodoItem> execute(Plan plan, StepRunner runner) throws Exception {
        for (TodoItem item : plan.todos()) {
            runner.run(item);   // 执行该步骤
            // 校验闭环：执行成功 → 勾选 completed
            todoList.replaceAll(itemsWith(item));
        }
        return todoList.items();
    }

    /** 把 item 标记为完成并合并回当前清单（保持顺序）。 */
    private List<TodoItem> itemsWith(TodoItem done) {
        java.util.ArrayList<TodoItem> merged = new java.util.ArrayList<>();
        for (TodoItem item : todoList.items()) {
            merged.add(item.content().equals(done.content())
                    ? done.withStatus(TodoStatus.COMPLETED) : item);
        }
        if (merged.stream().noneMatch(i -> i.content().equals(done.content()))) {
            merged.add(done.withStatus(TodoStatus.COMPLETED));
        }
        return merged;
    }
}
