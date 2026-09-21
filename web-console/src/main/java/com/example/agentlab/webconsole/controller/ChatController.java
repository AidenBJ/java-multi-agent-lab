package com.example.agentlab.webconsole.controller;

import com.example.agentlab.webconsole.service.ChatService;
import com.example.agentlab.webconsole.service.DemoRegistry;
import com.example.agentlab.webconsole.service.LogBuffer;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API：列出 Demo + 对话接口 + 日志接口。
 *
 * @author guoxiangyue
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;
    private final LogBuffer logBuffer;

    public ChatController(ChatService chatService, LogBuffer logBuffer) {
        this.chatService = chatService;
        this.logBuffer = logBuffer;
    }

    /**
     * 列出所有可体验的 Demo。
     */
    @GetMapping("/demos")
    public List<Map<String, Object>> listDemos() {
        return DemoRegistry.listDemos();
    }

    /**
     * 对话接口。
     *
     * @param demoId    Demo ID
     * @param sessionId 会话 ID
     * @param input     用户输入
     * @return 回复
     */
    @GetMapping("/chat")
    public Map<String, String> chat(
            @RequestParam("demoId") String demoId,
            @RequestParam("sessionId") String sessionId,
            @RequestParam("input") String input) {
        String reply = chatService.chat(demoId, sessionId, input);
        return Map.of("reply", reply);
    }

    /**
     * 获取增量日志（前端轮询用）。
     */
    @GetMapping("/logs")
    public List<String> getLogs() {
        return logBuffer.getIncrement();
    }

    /**
     * 清空日志。
     */
    @PostMapping("/logs/clear")
    public Map<String, String> clearLogs() {
        logBuffer.clear();
        return Map.of("status", "ok");
    }
}

