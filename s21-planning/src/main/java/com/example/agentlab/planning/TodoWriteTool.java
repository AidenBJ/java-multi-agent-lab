package com.example.agentlab.planning;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.List;

/**
 * todo_write 工具（AiServices 版）：模型通过工具调用更新任务清单。
 *
 * <p>关键洞察（对齐参考工程）：它不给 Agent 增加任何执行能力，增加的是规划能力。</p>
  * @author guoxiangyue
 */
public class TodoWriteTool {

    private final TodoList todoList;

    public TodoWriteTool(TodoList todoList) {
        this.todoList = todoList;
    }

    @Tool("创建并管理任务清单：接收带状态的待办列表（content + status: pending/in_progress/completed），整体替换当前清单并返回最新进度")
    public String updateTodos(@P("待办任务列表") List<TodoItem> todos) {
        todoList.replaceAll(todos);
        return todoList.render();
    }
}
