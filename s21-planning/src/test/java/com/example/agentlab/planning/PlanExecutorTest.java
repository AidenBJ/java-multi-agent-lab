package com.example.agentlab.planning;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 计划执行器离线测试：执行-校验闭环。
 */
class PlanExecutorTest {

    @Test
    void execute_runsEveryStep_andMarksCompleted() throws Exception {
        Plan plan = Plan.of("写报告", List.of(
                TodoItem.pending("收集数据"),
                TodoItem.pending("分析"),
                TodoItem.pending("成稿")
        ));
        TodoList list = new TodoList();
        list.replaceAll(plan.todos());

        List<String> executed = new ArrayList<>();
        new PlanExecutor(list).execute(plan, item -> executed.add(item.content()));

        assertThat(executed).containsExactly("收集数据", "分析", "成稿");
        assertThat(list.isAllCompleted()).isTrue();
        assertThat(list.completedCount()).isEqualTo(3);
    }

    @Test
    void execute_withEmptyPlan_keepsListEmpty() throws Exception {
        Plan plan = Plan.of("空计划", List.of());
        TodoList list = new TodoList();

        new PlanExecutor(list).execute(plan, item -> {
        });

        assertThat(list.isAllCompleted()).isFalse(); // 空计划不算"全部完成"
        assertThat(list.size()).isZero();
    }

    @Test
    void execute_propagatesStepException() {
        Plan plan = Plan.of("失败计划", List.of(TodoItem.pending("会失败的一步")));
        TodoList list = new TodoList();

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        new PlanExecutor(list).execute(plan, item -> {
                            throw new IllegalStateException("步骤执行失败");
                        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("步骤执行失败");
    }
}
