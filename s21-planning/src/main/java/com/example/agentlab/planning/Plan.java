package com.example.agentlab.planning;

import java.util.List;

/**
 * 结构化计划：目标 + 步骤清单（AiServices 返回类型 → 自动 JSON schema）。
  * @author guoxiangyue
 */
public record Plan(String goal, List<TodoItem> todos) {

    public static Plan of(String goal, List<TodoItem> todos) {
        return new Plan(goal, todos);
    }
}
