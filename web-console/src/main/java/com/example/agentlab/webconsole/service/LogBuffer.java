package com.example.agentlab.webconsole.service;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 内存日志缓冲区：存最近 N 条日志，供前端轮询展示。
 *
 * <p>
 * 工具类也可以通过静态方法直接写日志，不用注入。
 * </p>
 *
 * @author guoxiangyue
 */
@Component
public class LogBuffer {

    /** 最多保留 200 条日志 */
    private static final int MAX_SIZE = 200;

    /** 静态实例（工具类直接用 LogBuffer.log() 写日志） */
    private static volatile LogBuffer instance;

    /** 日志队列（线程安全） */
    private final Deque<String> logs = new ArrayDeque<>();

    /** 已返回给前端的日志条数（增量用） */
    private int lastSize = 0;

    /**
     * Spring 初始化时设置静态实例。
     */
    public LogBuffer() {
        instance = this;
    }

    /**
     * 工具类直接调用的静态日志方法。
     */
    public static void log(String message) {
        if (instance != null) {
            instance.add(message);
        }
        // 同时输出到控制台（方便调试）
        System.out.println(message);
    }

    /**
     * 添加一条日志。
     */
    public synchronized void add(String log) {
        logs.addLast(log);
        if (logs.size() > MAX_SIZE) {
            logs.removeFirst();
        }
    }

    /**
     * 获取所有日志。
     */
    public synchronized List<String> getAll() {
        return new ArrayList<>(logs);
    }

    /**
     * 获取增量日志（上次调用后新增的）。
     */
    public synchronized List<String> getIncrement() {
        List<String> all = new ArrayList<>(logs);
        int from = Math.min(lastSize, all.size());
        lastSize = all.size();
        return all.subList(from, all.size());
    }

    /**
     * 清空日志。
     */
    public synchronized void clear() {
        logs.clear();
        lastSize = 0;
    }
}
