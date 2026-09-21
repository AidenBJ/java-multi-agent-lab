package com.example.agentlab.webconsole.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 计算器工具（S1.2 演示用）。
 *
 * @author guoxiangyue
 */
class CalculatorTools {

    /**
     * 加法。
     */
    public static int add(int a, int b) {
        return a + b;
    }

    /**
     * 减法。
     */
    public static int subtract(int a, int b) {
        return a - b;
    }
}

/**
 * 时间工具（S1.2 演示用）。
 *
 * @author guoxiangyue
 */
class TimeTools {

    /**
     * 获取当前时间。
     */
    public static String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
