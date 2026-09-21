package com.example.agentlab.planning;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * todo_write 参数的 JSON 编解码（拆开看本质：手写循环里工具参数解析自己做）。
 *
 * <p>模型调用 todo_write 时传 {@code {"todos":[{"content":"...","status":"pending|in_progress|completed"}]}}。</p>
  * @author guoxiangyue
 */
public final class TodoJsonCodec {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 解析工具参数 JSON → 待办列表（容忍大小写与缺失字段）。 */
    public static List<TodoItem> parseTodos(String json) throws Exception {
        TodosWrapper wrapper = JSON.readValue(json, TodosWrapper.class);
        return wrapper.todos() == null ? List.of() : wrapper.todos();
    }

    /** 把待办列表序列化为工具参数格式（测试/调试用）。 */
    public static String toJson(List<TodoItem> todos) throws Exception {
        return JSON.writeValueAsString(new TodosWrapper(todos));
    }

    /** {"todos":[...]} 包装结构。 */
    record TodosWrapper(List<TodoItem> todos) {
    }
}
