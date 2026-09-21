package com.example.agentlab.agentloop;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;

/**
 * 测试桩：可编程的 ChatModel，按预设序列返回响应（先工具调用，再最终文本）。
 * 用于无网络地验证 AgentLoop 的循环逻辑。
 */
class FakeChatModel implements ChatModel {

    private final List<ChatResponse> responses;
    int callCount;

    FakeChatModel(List<ChatResponse> responses) {
        this.responses = responses;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        ChatResponse response = responses.get(Math.min(callCount, responses.size() - 1));
        callCount++;
        return response;
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        return chat(ChatRequest.builder().messages(messages).build());
    }

    /** 第一轮返回工具调用请求。 */
    static ChatResponse toolCallResponse(String id, String toolName, String arguments) {
        var request = dev.langchain4j.agent.tool.ToolExecutionRequest.builder()
                .id(id)
                .name(toolName)
                .arguments(arguments)
                .build();
        return ChatResponse.builder().aiMessage(new AiMessage(List.of(request))).build();
    }

    /** 返回纯文本最终回答。 */
    static ChatResponse finalResponse(String text) {
        return ChatResponse.builder().aiMessage(new AiMessage(text)).build();
    }
}
