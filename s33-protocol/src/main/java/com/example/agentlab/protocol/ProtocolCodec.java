package com.example.agentlab.protocol;

import dev.langchain4j.internal.Json;

/**
 * 信封与 payload 的 JSON 编解码（复用 langchain4j 自带 Json）。
  * @author guoxiangyue
 */
public class ProtocolCodec {

    public String toJson(Object obj) {
        return Json.toJson(obj);
    }

    /** 按消息类型把 payload 解析为对应 record；类型不符抛 E_PAYLOAD_MISMATCH。 */
    public Object parsePayload(MessageType type, String payload) {
        try {
            return switch (type) {
                case REQUEST -> Json.fromJson(payload, Payloads.TaskRequest.class);
                case RESULT -> Json.fromJson(payload, Payloads.TaskResult.class);
                case ERROR -> Json.fromJson(payload, Payloads.TaskError.class);
                case HANDOFF -> Json.fromJson(payload, Payloads.Handoff.class);
            };
        } catch (Exception e) {
            throw new ProtocolException(ErrorCode.E_PAYLOAD_MISMATCH,
                    type + " 的 payload 解析失败: " + e.getMessage());
        }
    }

    /** 把 payload record 重新包成 JSON 字符串。 */
    public String toPayloadJson(Object payload) {
        return Json.toJson(payload);
    }
}
