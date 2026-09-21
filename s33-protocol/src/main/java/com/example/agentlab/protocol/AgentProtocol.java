package com.example.agentlab.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 间协议总线：注册成员 → 校验信封 → 路由分发 → 返回响应。
 * 任何不合法消息都被拦截并返回标准 ERROR 信封。
 *
 * @author guoxiangyue
 */
public class AgentProtocol {

    /** 成员注册表：name → TeamMember */
    private final Map<String, TeamMember> members = new HashMap<>();
    /** 消息校验器 */
    private final MessageValidator validator = new MessageValidator();
    /** JSON 编解码器 */
    private final ProtocolCodec codec = new ProtocolCodec();

    /**
     * 注册一个团队成员。
     *
     * @param member 团队成员
     */
    public void register(TeamMember member) {
        members.put(member.name(), member);
    }

    /** 获取编解码器（用于构造响应信封）。 */
    public ProtocolCodec codec() {
        return codec;
    }

    /**
     * 发送一条消息：校验 → 路由 → 成员处理 → 返回响应。
     * 校验失败时不路由，直接返回标准 ERROR 信封。
     *
     * @param env 信封
     * @return 响应信封（RESULT / ERROR / HANDOFF）
     */
    public Envelope send(Envelope env) {
        try {
            // 先校验信封（必填字段、版本、接收方、payload 结构）
            validator.validate(env, members.keySet());
            // 路由到接收方
            TeamMember recipient = members.get(env.to());
            return recipient.handle(env, codec);
        } catch (ProtocolException e) {
            // 任何协议异常都兜底成标准 ERROR 信封
            return errorReply(env, e.code(), e.getMessage());
        }
    }

    /**
     * 构造标准错误响应信封。
     */
    private Envelope errorReply(Envelope request, ErrorCode code, String detail) {
        Payloads.TaskError err = new Payloads.TaskError(code, detail, false);
        return new Envelope(Envelope.CURRENT_VERSION, request.messageId(),
                "protocol", request.from(), MessageType.ERROR, codec.toJson(err));
    }
}
