package com.example.agentlab.planning;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 待办状态：pending（待办）/ in_progress（进行中）/ completed（已完成）。
 *
 * <p>JSON 反序列化宽容大小写（模型可能填 "pending" 或 "PENDING"）。</p>
  * @author guoxiangyue
 */
public enum TodoStatus {
    PENDING, IN_PROGRESS, COMPLETED;

    @JsonCreator
    public static TodoStatus from(String value) {
        if (value == null) {
            return PENDING;
        }
        return valueOf(value.trim().toUpperCase());
    }
}
