package com.example.agentlab.planning;

/**
 * 一条待办：内容 + 状态（JSON 字段名与模型工具 schema 对齐）。
  * @author guoxiangyue
 */
public record TodoItem(String content, TodoStatus status) {

    /** 缺 status 时默认 PENDING（JSON 反序列化 null 归一化）。 */
    public TodoItem {
        if (status == null) {
            status = TodoStatus.PENDING;
        }
    }

    /** 便捷构造：默认 pending。 */
    public static TodoItem pending(String content) {
        return new TodoItem(content, TodoStatus.PENDING);
    }

    public TodoItem withStatus(TodoStatus newStatus) {
        return new TodoItem(content, newStatus);
    }
}
