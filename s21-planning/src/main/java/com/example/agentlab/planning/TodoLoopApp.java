package com.example.agentlab.planning;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;

/**
 * S2.1 Demo A（对齐参考工程 s05）：todo_write 工具 + nag reminder。
 *
 * <p>模型先列计划（todo_write 全 pending）→ 逐步执行并更新状态 → 连续几轮不更新会收到提醒。</p>
 *
 * <pre>
 *   mvn -q -pl planning exec:java "-Dexec.mainClass=com.example.agentlab.planning.TodoLoopApp"
 * </pre>
  * @author guoxiangyue
 */
public class TodoLoopApp {

    public static void main(String[] args) {
        ChatModel model = ModelFactory.createDefaultChatModel();

        String task = args.length > 0
                ? String.join(" ", args)
                : "请用 todo_write 工具为'写一篇多 Agent 技术分享的提纲'制定 4 步计划，然后逐项完成并更新状态。";

        String answer = new TodoLoop(model, new TodoList()).run(task);
        System.out.println("=== 最终回答 ===");
        System.out.println(answer);
    }
}
