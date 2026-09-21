package com.example.agentlab.tooluse;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * S1.2 Demo：把工具注册给 Agent，让它能"计算 + 写文件 + 查网页"。
 *
 * <pre>
 *   mvn -q -pl tool-use exec:java "-Dexec.mainClass=com.example.agentlab.tooluse.ToolDemo"
 * </pre>
  * @author guoxiangyue
 */
public class ToolDemo {

    /** AiServices 接口：一个普通方法声明即对话入口。 */
    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        ChatModel model = ModelFactory.createDefaultChatModel();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                // 把工具对象注册给 Agent（@Tool 方法自动生成工具规格）
                .tools(new CalculatorTool(), new FileTools(), new HttpTool())
                .build();

        System.out.println("=== 工具 Agent Demo ===");
        String task = args.length > 0
                ? String.join(" ", args)
                : "请计算 123*456，然后把结果写入文件 result.txt，最后读取该文件确认内容。";
        System.out.println("任务: " + task);
        System.out.println("Agent: " + assistant.chat(task));
    }
}
