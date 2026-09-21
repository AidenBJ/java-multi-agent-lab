package com.example.agentlab.agentloop;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.ChatModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * S1.1 手写 Agent Loop（拆开框架看本质）。
 *
 * <pre>
 *   messages → LLM → 有 tool_use?
 *                         ├─ 否 → 返回文本（循环结束）
 *                         └─ 是 → 执行工具 → 结果回填 messages → 回到 LLM
 * </pre>
 *
 * <p>循环永远不变，机制（工具、权限、Hooks…）往循环上叠加。这里刻意不用 AiServices 高层 API，
 * 用 langchain4j 底层 ChatRequest/ChatResponse 把每一步看清楚。</p>
  * @author guoxiangyue
 */
public class AgentLoop {

    /** 工具句柄：接收工具调用的 arguments（JSON 字符串），返回文本结果。 */
    public interface ToolHandler extends Function<String, String> {
    }

    private final ChatModel model;
    private final Map<String, ToolHandler> tools;
    private final List<AgentObserver> observers;
    private final int maxIterations;
    private final List<ChatMessage> messages = new ArrayList<>();

    public AgentLoop(ChatModel model, Map<String, ToolHandler> tools, int maxIterations) {
        this(model, tools, maxIterations, List.of());
    }

    public AgentLoop(ChatModel model, Map<String, ToolHandler> tools, int maxIterations,
                     List<AgentObserver> observers) {
        this.model = model;
        this.tools = new HashMap<>(tools);
        this.maxIterations = maxIterations;
        this.observers = List.copyOf(observers);
    }

    /** 运行一轮 Agent 任务，返回最终文本答案。 */
    public String run(String userInput) {
        messages.add(new UserMessage(userInput));

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            notifyIterationStart(iteration);

            // 1. 调用模型（传入当前全部消息）
            var response = model.chat(messages);
            AiMessage ai = response.aiMessage();
            messages.add(ai);
            notifyModelResponse(iteration, ai.text(), ai.hasToolExecutionRequests());

            // 2. 没有工具调用 → 这就是最终答案
            if (!ai.hasToolExecutionRequests()) {
                notifyLoopFinished(ai.text());
                return ai.text();
            }

            // 3. 执行模型请求的工具，把结果回填进消息
            for (ToolExecutionRequest request : ai.toolExecutionRequests()) {
                String result = executeTool(request);
                messages.add(new ToolExecutionResultMessage(request.id(), request.name(), result));
            }
        }
        throw new IllegalStateException("超过最大迭代次数 " + maxIterations + "，任务未完成");
    }

    private String executeTool(ToolExecutionRequest request) {
        ToolHandler handler = tools.get(request.name());
        long start = System.nanoTime();
        notifyToolStart(request.name(), request.arguments());
        String result;
        if (handler == null) {
            result = "错误：未知工具 " + request.name();
        } else {
            try {
                result = handler.apply(request.arguments());
            } catch (Exception e) {
                result = "错误：工具执行失败 - " + e.getMessage();
            }
        }
        notifyToolEnd(request.name(), result, (System.nanoTime() - start) / 1_000_000);
        return result;
    }

    // ---------- 观察者触发（S1.4 的插口） ----------

    private void notifyIterationStart(int i) {
        observers.forEach(o -> o.onIterationStart(i));
    }

    private void notifyModelResponse(int i, String text, boolean hasTools) {
        observers.forEach(o -> o.onModelResponse(i, text, hasTools));
    }

    private void notifyToolStart(String name, String args) {
        observers.forEach(o -> o.onToolStart(name, args));
    }

    private void notifyToolEnd(String name, String result, long millis) {
        observers.forEach(o -> o.onToolEnd(name, result, millis));
    }

    private void notifyLoopFinished(String answer) {
        observers.forEach(o -> o.onLoopFinished(answer));
    }
}
