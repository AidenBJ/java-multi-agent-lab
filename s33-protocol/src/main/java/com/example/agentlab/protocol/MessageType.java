package com.example.agentlab.protocol;

/**
 * 消息类型。
  * @author guoxiangyue
 */
public enum MessageType {
    /** 请求任务。 */
    REQUEST,
    /** 任务结果。 */
    RESULT,
    /** 错误（标准错误码）。 */
    ERROR,
    /** 明确"交给谁、为什么"的转交。 */
    HANDOFF
}
