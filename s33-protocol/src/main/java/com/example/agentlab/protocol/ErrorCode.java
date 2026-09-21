package com.example.agentlab.protocol;

/**
 * 标准错误码。
  * @author guoxiangyue
 */
public enum ErrorCode {
    /** 消息格式不合法（缺字段、类型不符）。 */
    E_INVALID_MESSAGE,
    /** 协议版本不兼容。 */
    E_UNSUPPORTED_VERSION,
    /** 目标 Agent 不存在。 */
    E_UNKNOWN_RECIPIENT,
    /** payload 与消息类型不匹配。 */
    E_PAYLOAD_MISMATCH,
    /** 业务处理失败。 */
    E_HANDLING_FAILED
}
