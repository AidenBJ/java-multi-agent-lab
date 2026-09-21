package com.example.agentlab.planning;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 带规划机制的 Agent 循环（对齐参考工程 s05：todo_write 工具 + nag reminder）。
 *
 * <p>在 S1.1 的手写循环上叠加两个机制，循环本身不变：</p>
 * <ol>
 *   <li>todo_write 工具：模型可随时更新任务清单（只规划，不执行）；</li>
 *   <li>nag reminder：连续 {@link #NAG_AFTER_ROUNDS} 轮没更新清单，注入提醒。</li>
 * </ol>
  * @author guoxiangyue
 */
public class TodoLoop {

    /** 连续多少轮未调用 todo_write 就注入提醒（教学版机制，参考工程同款）。 */
    public static final int NAG_AFTER_ROUNDS = 3;

    public static final int MAX_ITERATIONS = 15;

    private final ChatModel model;
    private final TodoList todoList;
    private final List<ChatMessage> messages = new ArrayList<>();
    private int roundsSinceTodoUpdate = 0;

    public TodoLoop(ChatModel model, TodoList todoList) {
        this.model = model;
        this.todoList = todoList;
    }

    /** 运行一轮带规划的任务，返回最终回答。 */
    public String run(String task) {
        messages.add(new UserMessage(task));
        System.out.println("[系统] 任务: " + task);

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            maybeNag();

            var response = model.chat(messages);
            AiMessage ai = response.aiMessage();
            messages.add(ai);

            if (!ai.hasToolExecutionRequests()) {
                System.out.println("[系统] 循环结束（模型给出最终回答）");
                return ai.text();
            }

            for (ToolExecutionRequest request : ai.toolExecutionRequests()) {
                String result = dispatch(request);
                messages.add(new ToolExecutionResultMessage(request.id(), request.name(), result));
                System.out.println(result);
            }
        }
        throw new IllegalStateException("超过最大迭代次数 " + MAX_ITERATIONS + "，任务未完成");
    }

    /** 分发工具调用：目前只有 todo_write。 */
    private String dispatch(ToolExecutionRequest request) {
        if ("todo_write".equals(request.name())) {
            roundsSinceTodoUpdate = 0;
            try {
                todoList.replaceAll(TodoJsonCodec.parseTodos(request.arguments()));
                return todoList.render();
            } catch (Exception e) {
                return "解析失败: " + e.getMessage()
                        + "，请使用 {\"todos\":[{\"content\":\"...\",\"status\":\"pending|in_progress|completed\"}]}";
            }
        }
        return "未知工具: " + request.name();
    }

    /** 连续 N 轮没更新清单 → 注入 reminder。 */
    private void maybeNag() {
        if (roundsSinceTodoUpdate >= NAG_AFTER_ROUNDS) {
            messages.add(new UserMessage(
                    "<reminder>你已经连续几轮没有更新任务清单了。请调用 todo_write 更新任务进度（标注 pending/in_progress/completed）。</reminder>"));
            System.out.println("[系统] ⚠ 注入 reminder：请更新任务清单");
            roundsSinceTodoUpdate = 0;
        }
        roundsSinceTodoUpdate++;
    }
}
