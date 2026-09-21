package com.example.agentlab.minimalchat;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/**
 * S0.2 流式多轮对话：在 ChatCli 基础上加"打字机"效果（边生成边输出）。
 *
 * <p>通过 {@link StreamingChatResponseHandler} 接收增量 token。</p>
 *
 * <pre>
 *   mvn -q -pl minimal-chat exec:java "-Dexec.mainClass=com.example.agentlab.minimalchat.StreamingCli"
 * </pre>
  * @author guoxiangyue
 */
public class StreamingCli {

    public static void main(String[] args) throws InterruptedException {
        StreamingChatModel model = ModelFactory.createStreamingChatModel(
                com.example.agentlab.common.LlmConfig.fromEnv());
        MessageWindowChatMemory memory = MessageWindowChatMemory.withMaxMessages(ChatCli.MAX_MESSAGES);

        System.out.println("=== 流式多轮对话（输入 exit 退出）===");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("你: ");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input.trim())) {
                    break;
                }
                memory.add(new UserMessage(input));
                List<ChatMessage> messages = memory.messages();

                CountDownLatch done = new CountDownLatch(1);
                StringBuilder full = new StringBuilder();
                System.out.print("Agent: ");
                model.chat(messages, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String token) {
                        System.out.print(token);   // 增量输出
                        full.append(token);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse response) {
                        System.out.println();
                        memory.add(response.aiMessage());
                        done.countDown();
                    }

                    @Override
                    public void onError(Throwable error) {
                        System.err.println("\n[流式错误] " + error.getMessage());
                        done.countDown();
                    }
                });
                done.await();
            }
        }
    }
}
