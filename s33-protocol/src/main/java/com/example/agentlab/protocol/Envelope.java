package com.example.agentlab.protocol;

/**
 * 信封：所有 Agent 间消息的固定外壳。
 *
 * @param version   协议版本（如 "1.0"）
 * @param messageId 消息唯一 ID
 * @param from      发送方 Agent 名
 * @param to        接收方 Agent 名
 * @param type      消息类型
 * @param payload   JSON 字符串，内容按 type 解析为对应 payload record
  * @author guoxiangyue
 */
public record Envelope(
        String version,
        String messageId,
        String from,
        String to,
        MessageType type,
        String payload) {

    public static final String CURRENT_VERSION = "1.0";
}
