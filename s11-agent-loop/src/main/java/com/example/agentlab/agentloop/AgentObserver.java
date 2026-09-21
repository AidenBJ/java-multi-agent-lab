package com.example.agentlab.agentloop;

/**
 * Agent 循环观察者（S1.4 Hooks 的插口）。
 *
 * <p>挂在 {@link AgentLoop} 上，在循环的关键节点收到事件，不改动循环代码即可扩展能力。</p>
  * @author guoxiangyue
 */
public interface AgentObserver {

    /** 每轮迭代开始（模型收到全部消息前）。 */
    default void onIterationStart(int iteration) {
    }

    /** 模型返回后：包含助手消息（可能带工具调用）。 */
    default void onModelResponse(int iteration, String modelText, boolean hasToolCalls) {
    }

    /** 单个工具即将执行。 */
    default void onToolStart(String toolName, String arguments) {
    }

    /** 单个工具执行完成。 */
    default void onToolEnd(String toolName, String result, long elapsedMillis) {
    }

    /** 循环结束（拿到最终答案或失败）。 */
    default void onLoopFinished(String finalAnswer) {
    }
}
