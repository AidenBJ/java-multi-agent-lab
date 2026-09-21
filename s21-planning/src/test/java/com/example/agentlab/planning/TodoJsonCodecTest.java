package com.example.agentlab.planning;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * todo_write 工具参数 JSON 编解码测试（离线）。
 */
class TodoJsonCodecTest {

    @Test
    void parseTodos_acceptsModelOutput() throws Exception {
        String json = """
                {"todos":[
                  {"content":"第一步：收集资料","status":"pending"},
                  {"content":"第二步：写提纲","status":"in_progress"},
                  {"content":"第三步：成稿","status":"completed"}
                ]}""";

        List<TodoItem> todos = TodoJsonCodec.parseTodos(json);

        assertThat(todos).hasSize(3);
        assertThat(todos.get(0).content()).isEqualTo("第一步：收集资料");
        assertThat(todos.get(0).status()).isEqualTo(TodoStatus.PENDING);
        assertThat(todos.get(1).status()).isEqualTo(TodoStatus.IN_PROGRESS);
        assertThat(todos.get(2).status()).isEqualTo(TodoStatus.COMPLETED);
    }

    @Test
    void parseTodos_toleratesUppercaseAndMissing() throws Exception {
        String json = """
                {"todos":[
                  {"content":"A","status":"PENDING"},
                  {"content":"B","status":"Completed"},
                  {"content":"C"}
                ]}""";

        List<TodoItem> todos = TodoJsonCodec.parseTodos(json);

        assertThat(todos).hasSize(3);
        assertThat(todos.get(0).status()).isEqualTo(TodoStatus.PENDING);
        assertThat(todos.get(1).status()).isEqualTo(TodoStatus.COMPLETED);
        // 缺 status 默认 pending
        assertThat(todos.get(2).status()).isEqualTo(TodoStatus.PENDING);
    }

    @Test
    void parseTodos_roundTrip() throws Exception {
        List<TodoItem> original = List.of(TodoItem.pending("x"), new TodoItem("y", TodoStatus.COMPLETED));

        String json = TodoJsonCodec.toJson(original);
        List<TodoItem> back = TodoJsonCodec.parseTodos(json);

        assertThat(back).isEqualTo(original);
    }
}
