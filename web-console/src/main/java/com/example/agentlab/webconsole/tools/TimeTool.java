package com.example.agentlab.webconsole.tools;

import com.example.agentlab.webconsole.service.LogBuffer;
import dev.langchain4j.agent.tool.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具（真正的 @Tool 注解，LLM 自主决定是否调用）。
 *
 * <p>
 * 每次调用都会打印详细的证据日志，让你清楚看到：
 * 1. LLM 真的决定调用这个工具了
 * 2. 返回的真实系统时间是什么
 * 3. 调用耗时多少
 * </p>
 *
 * @author guoxiangyue
 */
public class TimeTool {

    /**
     * 获取当前系统时间。
     *
     * @return 当前时间字符串（格式 yyyy-MM-dd HH:mm:ss）
     */
    @Tool("获取当前系统时间（精确到秒）")
    public String getCurrentTime() {

        long start = System.currentTimeMillis();

        // 【证据打印】证明工具真的被调用了
        LogBuffer.log("========================================");
        LogBuffer.log("[工具调用证据] TimeTool.getCurrentTime() 被触发！");
        LogBuffer.log("  · 方法名: time.getCurrentTime");
        LogBuffer.log("  · 传入参数: 无");
        LogBuffer.log("  · 调用时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        long cost = System.currentTimeMillis() - start;
        LogBuffer.log("  · 返回结果: 当前系统时间 = " + time);
        LogBuffer.log("  · 调用耗时: " + cost + " ms");
        LogBuffer.log("========================================");

        return time;
    }
}
