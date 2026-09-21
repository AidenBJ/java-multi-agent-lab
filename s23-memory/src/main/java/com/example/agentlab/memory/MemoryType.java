package com.example.agentlab.memory;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 记忆类型（对齐参考工程 s09）：user / feedback / project / reference。
  * @author guoxiangyue
 */
public enum MemoryType {
    USER, FEEDBACK, PROJECT, REFERENCE;

    @JsonCreator
    public static MemoryType from(String value) {
        if (value == null) {
            return USER;
        }
        return valueOf(value.trim().toUpperCase());
    }
}
