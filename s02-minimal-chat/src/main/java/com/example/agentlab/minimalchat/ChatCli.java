package com.example.agentlab.minimalchat;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * S0.2 命令行多轮对话：用 ChatMemory（窗口记忆）记住上下文。
 *
 * <p>这是"最简单的一个 Agent"——没有工具、没有图，只有 输入→记忆→模型→输出 的循环。</p>
 *
 * <pre>
 *   mvn -q -pl s02-minimal-chat -am exec:java "-Dexec.mainClass=com.example.agentlab.minimalchat.ChatCli"
 * </pre>
 *
 * @author guoxiangyue
 */
public class ChatCli {

    /** 记忆窗口大小：最多保留 20 条消息（用户 + 助手），超出自动丢弃最旧的。 */
    static final int MAX_MESSAGES = 20;

    public static void main(String[] args) {
        // Windows 控制台默认 GBK，显式切 UTF-8 避免中文乱码
        wrapConsoleToUtf8();

        ChatModel model = ModelFactory.createDefaultChatModel();
        // 窗口记忆：框架维护消息历史，我们只负责追加
        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES);

        System.out.println("=== 多轮对话（输入 exit 退出；模型将记住前文）===");
        // Scanner 显式用 UTF-8 读控制台输入
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("你: ");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input.trim())) {
                    break;
                }
                // 1. 用户消息进记忆
                memory.add(new UserMessage(input));
                // 2. 把当前全部记忆交给模型
                List<ChatMessage> messages = memory.messages();
                ChatResponse response = model.chat(messages);
                // 3. 助手回复进记忆
                memory.add(response.aiMessage());
                System.out.println("Agent: " + response.aiMessage().text());
            }
        }
    }

    /**
     * 把控制台输出流包装成 UTF-8，避免中文乱码。
     */
    private static void wrapConsoleToUtf8() {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // 忽略失败，不影响主流程
        }
    }
}
