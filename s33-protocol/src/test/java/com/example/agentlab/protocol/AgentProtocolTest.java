package com.example.agentlab.protocol;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Agent 间协议离线测试：合法交互、错误拒绝、handoff。
 */
class AgentProtocolTest {

    private AgentProtocol build() {
        AgentProtocol p = new AgentProtocol();
        p.register(new TeamMember() {
            public String name() { return "writer"; }
            public Envelope handle(Envelope req, ProtocolCodec codec) {
                Payloads.TaskResult r = new Payloads.TaskResult(req.messageId(), "ok", true);
                return new Envelope(Envelope.CURRENT_VERSION, req.messageId(),
                        "writer", req.from(), MessageType.RESULT, codec.toJson(r));
            }
        });
        return p;
    }

    @Test
    void validRequest_returnsResult() {
        AgentProtocol p = build();
        ProtocolCodec codec = p.codec();
        Envelope req = new Envelope(Envelope.CURRENT_VERSION, "m1", "user", "writer",
                MessageType.REQUEST, codec.toJson(new Payloads.TaskRequest("写文章", "text")));

        Envelope resp = p.send(req);

        assertThat(resp.type()).isEqualTo(MessageType.RESULT);
        Payloads.TaskResult r = (Payloads.TaskResult) codec.parsePayload(MessageType.RESULT, resp.payload());
        assertThat(r.content()).isEqualTo("ok");
    }

    @Test
    void unknownRecipient_rejectedWithError() {
        AgentProtocol p = build();
        ProtocolCodec codec = p.codec();
        Envelope bad = new Envelope(Envelope.CURRENT_VERSION, "m2", "user", "ghost",
                MessageType.REQUEST, "{}");

        Envelope resp = p.send(bad);

        assertThat(resp.type()).isEqualTo(MessageType.ERROR);
        Payloads.TaskError e = (Payloads.TaskError) codec.parsePayload(MessageType.ERROR, resp.payload());
        assertThat(e.code()).isEqualTo(ErrorCode.E_UNKNOWN_RECIPIENT);
    }

    @Test
    void unsupportedVersion_rejected() {
        AgentProtocol p = build();
        ProtocolCodec codec = p.codec();
        Envelope old = new Envelope("0.9", "m3", "user", "writer",
                MessageType.REQUEST, "{}");

        Envelope resp = p.send(old);

        assertThat(resp.type()).isEqualTo(MessageType.ERROR);
        Payloads.TaskError e = (Payloads.TaskError) codec.parsePayload(MessageType.ERROR, resp.payload());
        assertThat(e.code()).isEqualTo(ErrorCode.E_UNSUPPORTED_VERSION);
    }

    @Test
    void payloadUnparseable_rejected() {
        AgentProtocol p = build();
        ProtocolCodec codec = p.codec();
        // REQUEST 类型传不以 { 开头的非法 payload（走协议层结构校验）
        Envelope bad = new Envelope(Envelope.CURRENT_VERSION, "m4", "user", "writer",
                MessageType.REQUEST, "not-json");

        Envelope resp = p.send(bad);

        assertThat(resp.type()).isEqualTo(MessageType.ERROR);
        Payloads.TaskError e = (Payloads.TaskError) codec.parsePayload(MessageType.ERROR, resp.payload());
        assertThat(e.code()).isEqualTo(ErrorCode.E_PAYLOAD_MISMATCH);
    }

    @Test
    void handoff_forwardedToAnotherMember() {
        AgentProtocol p = build();
        p.register(new TeamMember() {
            public String name() { return "coder"; }
            public Envelope handle(Envelope req, ProtocolCodec codec) {
                // coder 收到 HANDOFF：解析 Handoff，执行其 context
                Payloads.Handoff h = (Payloads.Handoff) codec.parsePayload(MessageType.HANDOFF, req.payload());
                Payloads.TaskResult r = new Payloads.TaskResult(req.messageId(),
                        "coder 接手: " + h.context(), true);
                return new Envelope(Envelope.CURRENT_VERSION, req.messageId(),
                        "coder", req.from(), MessageType.RESULT, codec.toJson(r));
            }
        });
        ProtocolCodec codec = p.codec();

        // writer 发 HANDOFF 给 coder
        Payloads.Handoff h = new Payloads.Handoff("m5", "不是写作活", "coder", "写代码");
        Envelope handoff = new Envelope(Envelope.CURRENT_VERSION, "m5", "writer", "coder",
                MessageType.HANDOFF, codec.toJson(h));
        Envelope resp = p.send(handoff);

        // 路由确实到了 coder，且 coder 正常接手
        assertThat(resp.type()).isEqualTo(MessageType.RESULT);
        Payloads.TaskResult r = (Payloads.TaskResult) codec.parsePayload(MessageType.RESULT, resp.payload());
        assertThat(r.content()).contains("coder 接手");
    }
}
