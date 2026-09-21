package com.example.agentlab.planning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 任务清单状态机离线测试（纯逻辑，无网络）。
 */
class TodoListTest {

    @Test
    void replaceAll_keepsOrderAndCounts() {
        TodoList list = new TodoList();
        list.replaceAll(List.of(
                TodoItem.pending("写提纲"),
                new TodoItem("找资料", TodoStatus.IN_PROGRESS),
                new TodoItem("成稿", TodoStatus.COMPLETED)
        ));

        assertThat(list.size()).isEqualTo(3);
        assertThat(list.pendingCount()).isEqualTo(1);
        assertThat(list.inProgressCount()).isEqualTo(1);
        assertThat(list.completedCount()).isEqualTo(1);
        assertThat(list.isAllCompleted()).isFalse();
        assertThat(list.updateCount()).isEqualTo(1);
    }

    @Test
    void isAllCompleted_trueOnlyWhenAllDone() {
        TodoList list = new TodoList();
        list.replaceAll(List.of(
                new TodoItem("a", TodoStatus.COMPLETED),
                new TodoItem("b", TodoStatus.COMPLETED)
        ));
        assertThat(list.isAllCompleted()).isTrue();
    }

    @Test
    void render_showsProgressIcons() {
        TodoList list = new TodoList();
        list.replaceAll(List.of(
                TodoItem.pending("待办"),
                new TodoItem("进行中", TodoStatus.IN_PROGRESS),
                new TodoItem("完成", TodoStatus.COMPLETED)
        ));

        String rendered = list.render();

        assertThat(rendered)
                .contains("[ ] 待办")
                .contains("[▸] 进行中")
                .contains("[✓] 完成")
                .contains("已完成 1/3");
    }

    @Test
    void replaceAll_replacesPreviousItems() {
        TodoList list = new TodoList();
        list.replaceAll(List.of(TodoItem.pending("第一版")));
        list.replaceAll(List.of(TodoItem.pending("第二版")));

        assertThat(list.size()).isEqualTo(1);
        assertThat(list.items().get(0).content()).isEqualTo("第二版");
        assertThat(list.updateCount()).isEqualTo(2);
    }
}
