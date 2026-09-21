package com.example.agentlab.memory;

import java.time.Instant;

/**
 * 一条持久化记忆：唯一名 + 描述 + 类型 + 正文 + 创建时间。
 *
 * <p>对应参考工程 .memory/*.md 文件（YAML frontmatter 字段即 name/description/type/createdAt）。</p>
  * @author guoxiangyue
 */
public record MemoryRecord(String name, String description, MemoryType type, String body, Instant createdAt) {

    public static MemoryRecord of(String name, String description, MemoryType type, String body) {
        return new MemoryRecord(name, description, type, body, Instant.now());
    }

    public MemoryRecord withCreatedAt(Instant newCreatedAt) {
        return new MemoryRecord(name, description, type, body, newCreatedAt);
    }
}
