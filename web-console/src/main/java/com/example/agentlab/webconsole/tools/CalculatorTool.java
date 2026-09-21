package com.example.agentlab.webconsole.tools;

import com.example.agentlab.webconsole.service.LogBuffer;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 计算器工具（真正的 @Tool 注解，LLM 自主决定是否调用）。
 *
 * <p>
 * 每次调用都会打印详细的证据日志，让你清楚看到：
 * 1. LLM 真的决定调用这个工具了
 * 2. 传入的参数是什么
 * 3. 计算结果是什么
 * 4. 调用耗时多少
 * </p>
 *
 * @author guoxiangyue
 */
public class CalculatorTool {

    /**
     * 加法。
     *
     * @param a 第一个整数
     * @param b 第二个整数
     * @return 两个数的和
     */
    @Tool("计算两个整数的和")
    public int add(
            @P("第一个整数") int a,
            @P("第二个整数") int b) {

        long start = System.currentTimeMillis();

        // 【证据打印】证明工具真的被调用了
        LogBuffer.log("========================================");
        LogBuffer.log("[工具调用证据] CalculatorTool.add() 被触发！");
        LogBuffer.log("  · 方法名: calculator.add");
        LogBuffer.log("  · 传入参数: a=" + a + ", b=" + b);
        LogBuffer.log("  · 调用时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        int result = a + b;

        long cost = System.currentTimeMillis() - start;
        LogBuffer.log("  · 计算结果: " + a + " + " + b + " = " + result);
        LogBuffer.log("  · 调用耗时: " + cost + " ms");
        LogBuffer.log("========================================");

        return result;
    }

    /**
     * 减法。
     *
     * @param a 被减数
     * @param b 减数
     * @return 两个数的差
     */
    @Tool("计算两个整数的差")
    public int subtract(
            @P("被减数") int a,
            @P("减数") int b) {

        long start = System.currentTimeMillis();

        // 【证据打印】证明工具真的被调用了
        LogBuffer.log("========================================");
        LogBuffer.log("[工具调用证据] CalculatorTool.subtract() 被触发！");
        LogBuffer.log("  · 方法名: calculator.subtract");
        LogBuffer.log("  · 传入参数: a=" + a + ", b=" + b);
        LogBuffer.log("  · 调用时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        int result = a - b;

        long cost = System.currentTimeMillis() - start;
        LogBuffer.log("  · 计算结果: " + a + " - " + b + " = " + result);
        LogBuffer.log("  · 调用耗时: " + cost + " ms");
        LogBuffer.log("========================================");

        return result;
    }
}
