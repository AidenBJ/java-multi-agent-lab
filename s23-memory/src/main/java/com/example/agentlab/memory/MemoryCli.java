package com.example.agentlab.memory;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * S2.3 Demo：带长期记忆的对话 CLI。
 *
 * <ul>
 *   <li>每轮对话前：从 .memory/ 选择相关记忆注入（跨会话）；</li>
 *   <li>每轮对话后：提取新记忆写入 .memory/；</li>
 *   <li>记忆数量达阈值自动整理去重；</li>
 *   <li>重启进程后 .memory/ 仍在 → 新会话能加载（长期记忆）。</li>
 * </ul>
 *
 * <pre>
 *   mvn -q -pl memory exec:java "-Dexec.mainClass=com.example.agentlab.memory.MemoryCli"
 * </pre>
  * @author guoxiangyue
 */
public class MemoryCli {

    static final int MAX_RELEVANT_MEMORIES = 5;
    static final int CONSOLIDATE_THRESHOLD = 10;

    public static void main(String[] args) {
        ChatModel model = ModelFactory.createDefaultChatModel();

        Path memoryDir = Path.of(System.getProperty("user.dir"), ".memory");
        FileMemoryStore store = new FileMemoryStore(memoryDir);
        MemoryService service = new MemoryService(store,
                new LlmMemoryExtractor(model),
                new LlmMemorySelector(model),
                new LlmMemoryConsolidator(model)::consolidate,
                CONSOLIDATE_THRESHOLD);

        MessageWindowChatMemory chat = MessageWindowChatMemory.withMaxMessages(20);

        System.out.println("=== 记忆系统 Demo（长期记忆目录: " + memoryDir + "）===");
        System.out.println("命令: /list 查看记忆  /del <name> 删除  /clear 清空  /exit 退出");
        if (store.size() > 0) {
            System.out.println("[系统] 已有 " + store.size() + " 条记忆（跨会话保留，重启后仍可加载）");
        }

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("你: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }
                if (input.startsWith("/")) {
                    if (handleCommand(input, service, store)) {
                        return;
                    }
                    continue;
                }

                // 1. 选择相关记忆并注入（长期记忆 → 当前轮）
                List<MemoryRecord> relevant = service.selectRelevant(
                        renderRecent(chat.messages()) + "\n" + input, MAX_RELEVANT_MEMORIES);
                String userMessage = relevant.isEmpty()
                        ? input
                        : "【长期记忆】\n" + renderMemories(relevant) + "\n\n【当前输入】\n" + input;
                chat.add(new UserMessage(userMessage));

                // 2. 模型回复
                ChatResponse response = model.chat(chat.messages());
                chat.add(response.aiMessage());
                System.out.println("Agent: " + response.aiMessage().text());

                // 3. 提取新记忆（本轮对话）
                int saved = service.extractAndSave(renderRecent(chat.messages()));
                if (saved > 0) {
                    System.out.println("[记忆] 已保存 " + saved + " 条新记忆");
                }

                // 4. 整理（阈值触发）
                if (service.consolidateIfNeeded()) {
                    System.out.println("[记忆] 已触发整理（去重合并）");
                }
            }
        }
    }

    /** 处理命令，返回 true 表示退出。 */
    private static boolean handleCommand(String input, MemoryService service, FileMemoryStore store) {
        if (input.equals("/exit")) {
            System.out.println("再见！长期记忆保留在 " + store.dir() + "。");
            return true;
        }
        if (input.equals("/list")) {
            List<MemoryRecord> all = service.list();
            if (all.isEmpty()) {
                System.out.println("（暂无记忆）");
            } else {
                System.out.println(service.renderIndex());
            }
            return false;
        }
        if (input.equals("/clear")) {
            service.clear();
            System.out.println("已清空全部记忆。");
            return false;
        }
        if (input.startsWith("/del ")) {
            String name = input.substring(5).trim();
            System.out.println(service.delete(name) ? "已删除: " + name : "未找到: " + name);
            return false;
        }
        System.out.println("未知命令: " + input);
        return false;
    }

    static String renderMemories(List<MemoryRecord> memories) {
        StringBuilder sb = new StringBuilder();
        for (MemoryRecord m : memories) {
            sb.append("- ").append(m.description());
            if (!m.body().isBlank()) {
                sb.append(" (").append(m.body()).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    static String renderRecent(List<ChatMessage> messages) {
        StringBuilder sb = new StringBuilder();
        int from = Math.max(0, messages.size() - 10);
        for (ChatMessage m : messages.subList(from, messages.size())) {
            if (m instanceof UserMessage u) {
                sb.append("用户: ").append(u.singleText()).append("\n");
            } else if (m instanceof AiMessage a) {
                sb.append("助手: ").append(a.text()).append("\n");
            }
        }
        return sb.toString();
    }
}
