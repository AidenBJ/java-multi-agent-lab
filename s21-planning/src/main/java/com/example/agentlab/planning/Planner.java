package com.example.agentlab.planning;

import dev.langchain4j.service.UserMessage;

/**
 * 规划器：把"任务"变成"结构化计划"。
 *
 * <p>AiServices 根据返回类型 {@link Plan} 自动生成 JSON schema，
 * 模型按要求输出 {goal, todos[]}，框架解析为 record。</p>
  * @author guoxiangyue
 */
public interface Planner {

    Plan plan(@UserMessage String task);
}
