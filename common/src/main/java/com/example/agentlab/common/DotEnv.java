package com.example.agentlab.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 轻量级 .env 文件加载器。
 *
 * <p>从工程根目录的 .env 文件读 KEY=VALUE 对，作为环境变量的补充。
 * 真实系统环境变量优先；.env 文件只作为兜底。</p>
 *
 * @author guoxiangyue
 */
public final class DotEnv {

    private static final Map<String, String> CACHE = new HashMap<>();
    private static boolean loaded = false;

    private DotEnv() {
    }

    /**
     * 从工程根目录加载 .env 文件（只加载一次）。
     */
    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        // 尝试多个可能的 .env 路径（工程根目录）
        Path[] candidates = {
                Path.of(".env"),
                Path.of("E:/agent/.env"),
                Path.of("../.env")
        };
        for (Path p : candidates) {
            if (Files.exists(p)) {
                try {
                    for (String line : Files.readAllLines(p)) {
                        line = line.trim();
                        // 跳过空行和注释
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int idx = line.indexOf('=');
                        if (idx <= 0) {
                            continue;
                        }
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        // 去掉引号
                        if ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        CACHE.put(key, value);
                    }
                } catch (IOException e) {
                    // 读失败就忽略，用真实环境变量
                }
                break;
            }
        }
    }

    /**
     * 获取配置值：先查真实系统环境变量，没有再查 .env 文件。
     *
     * @param key 配置键
     * @return 值，不存在返回 null
     */
    public static String get(String key) {
        String sys = System.getenv(key);
        if (sys != null && !sys.isBlank()) {
            return sys;
        }
        load();
        return CACHE.get(key);
    }
}
