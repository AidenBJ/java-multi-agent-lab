package com.example.agentlab.subagent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * task 工具（对齐参考工程 s06）：主 Agent 用它 spawn 子 Agent。
  * @author guoxiangyue
 */
public class TaskTool {

    private final SubagentRunner runner;

    public TaskTool(SubagentRunner runner) {
        this.runner = runner;
    }

    @Tool("启动一个子 Agent 处理复杂子任务，返回其最终结论（子任务不能再委派）")
    public String task(@P("子任务描述（必须自包含，子 Agent 看不到其他上下文）") String description) {
        return runner.spawn(description);
    }
}
