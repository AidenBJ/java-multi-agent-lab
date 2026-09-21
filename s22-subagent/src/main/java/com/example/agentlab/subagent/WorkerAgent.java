package com.example.agentlab.subagent;

import dev.langchain4j.service.UserMessage;

/**
 * 工人 Agent 接口：接收一条自包含指令，返回结论。
 *
 * <p>每个工人是独立的 AiServices 实例（独立系统提示 + 无共享记忆）——上下文天然隔离，
 * 工人之间看不到彼此的任何对话。</p>
  * @author guoxiangyue
 */
public interface WorkerAgent {

    String execute(@UserMessage String task);
}
