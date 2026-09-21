package com.example.agentlab.contextcompact;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * S2.4 离线 Demo：构造一段长对话（多轮工具调用），跑四层压缩管线，打印压缩前后对比。
 *
 * <pre>
 *   mvn -q -pl context-compact exec:java "-Dexec.mainClass=com.example.agentlab.contextcompact.CompactDemo"
 * </pre>
  * @author guoxiangyue
 */
public class CompactDemo {

    public static void main(String[] args) {
        // 桩摘要器（离线，不调 LLM）
        HistorySummarizer stubSummarizer = messages ->
                "[离线摘要] 历史共 " + messages.size() + " 条消息已压缩为一段。";

        ContextCompactor compactor = new ContextCompactor(
                new ToolResultBudgetCompactor(200_000, Path.of(System.getProperty("user.dir"),
                        ".task-outputs", "tool-results")),
                new SnipCompactor(50, 3),
                new MicroCompactor(3),
                stubSummarizer,
                10_000 // token 阈值（≈ 40000 字符）
        );

        List<ChatMessage> messages = buildLongDialogue(40);
        System.out.println("=== S2.4 上下文压缩 Demo（离线）===");
        System.out.println("压缩前：" + messages.size() + " 条消息，"
                + MessageEstimator.totalChars(messages) + " 字符，≈"
                + MessageEstimator.estimate(messages) + " token");

        CompactionResult result = compactor.compact(messages);

        System.out.println("压缩后：" + result.messages().size() + " 条消息，"
                + result.afterChars() + " 字符");
        System.out.println("应用层数：" + result.layersApplied()
                + "（L4 LLM 摘要：" + (result.summarized() ? "是" : "否") + "）");
        System.out.println("节省：" + result.savedChars() + " 字符（≈"
                + (result.beforeChars() - result.afterChars()) / 4 + " token）");
        System.out.println("首条消息预览："
                + MessageEstimator.textOf(result.messages().get(0))
                        .substring(0, Math.min(60, MessageEstimator.textOf(result.messages().get(0)).length())));
    }

    /** 构造：头 3 条初始上下文 + N 轮（AiMessage 带工具调用 + 大工具结果）。 */
    static List<ChatMessage> buildLongDialogue(int toolRounds) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage("任务：整理这个项目的所有源码"));
        messages.add(new AiMessage("好的，我先列出目录。"));
        messages.add(new AiMessage("调用 read 工具读取文件",
                List.of(ToolExecutionRequest.builder().id("0").name("read").arguments("{}").build())));
        messages.add(new ToolExecutionResultMessage("0", "read", "文件内容首版".repeat(200)));

        for (int i = 1; i <= toolRounds; i++) {
            messages.add(new AiMessage("继续读取第 " + i + " 个文件",
                    List.of(ToolExecutionRequest.builder().id(String.valueOf(i)).name("read")
                            .arguments("{}").build())));
            messages.add(new ToolExecutionResultMessage(String.valueOf(i), "read",
                    "第 " + i + " 个文件的完整内容（很长）".repeat(300)));
        }
        return messages;
    }
}
