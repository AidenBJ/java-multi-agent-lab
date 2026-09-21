package com.example.agentlab.memory;

/**
 * 提取出的新记忆（无 createdAt——写入时由存储层填充）。
  * @author guoxiangyue
 */
public record NewMemory(String name, String description, MemoryType type, String body) {

    MemoryRecord toRecord() {
        return MemoryRecord.of(name, description, type, body);
    }
}
