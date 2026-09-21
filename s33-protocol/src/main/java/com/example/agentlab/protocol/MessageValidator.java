package com.example.agentlab.protocol;

import java.util.Set;

/**
 * 消息校验：不合法消息直接抛 ProtocolException（标准错误码）。
  * @author guoxiangyue
 */
public class MessageValidator {

    /** 兼容的协议版本。 */
    static final Set<String> SUPPORTED_VERSIONS = Set.of("1.0");

    /** 校验信封必填字段与版本；不合法抛 ProtocolException。 */
    public void validate(Envelope env, Set<String> knownRecipients) {
        requireNonBlank(env.version(), "version");
        requireNonBlank(env.messageId(), "messageId");
        requireNonBlank(env.from(), "from");
        requireNonBlank(env.to(), "to");
        if (env.type() == null) {
            throw new ProtocolException(ErrorCode.E_INVALID_MESSAGE, "type 为空");
        }
        if (!SUPPORTED_VERSIONS.contains(env.version())) {
            throw new ProtocolException(ErrorCode.E_UNSUPPORTED_VERSION,
                    "不支持的版本: " + env.version() + "，支持: " + SUPPORTED_VERSIONS);
        }
        if (!knownRecipients.contains(env.to())) {
            throw new ProtocolException(ErrorCode.E_UNKNOWN_RECIPIENT, "未知接收方: " + env.to());
        }
        // payload 结构校验：必须非空且像 JSON 对象（协议层兜底，不依赖下游解析严格性）
        if (env.payload() == null || !env.payload().trim().startsWith("{")) {
            throw new ProtocolException(ErrorCode.E_PAYLOAD_MISMATCH, "payload 不是合法 JSON 对象");
        }
        // payload 与 type 的匹配在分发前由 Codec 解析时再校验
    }

    private void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProtocolException(ErrorCode.E_INVALID_MESSAGE, field + " 为空");
        }
    }
}
