package com.example.agentlab.protocol;

/**
 * S3.3 离线 Demo：writer 与 coder 严格按协议交互，含 handoff 与错误拒绝。
 *
 * <pre>
 *   mvn -q -pl protocol exec:java "-Dexec.mainClass=com.example.agentlab.protocol.ProtocolDemo"
 * </pre>
  * @author guoxiangyue
 */
public class ProtocolDemo {

    /** writer：文章任务自己做；代码任务 handoff 给 coder。 */
    static TeamMember writer() {
        return new TeamMember() {
            @Override
            public String name() {
                return "writer";
            }

            @Override
            public Envelope handle(Envelope req, ProtocolCodec codec) {
                Payloads.TaskRequest task =
                        (Payloads.TaskRequest) codec.parsePayload(MessageType.REQUEST, req.payload());
                if (task.task().contains("代码")) {
                    Payloads.Handoff handoff = new Payloads.Handoff(
                            req.messageId(), "代码任务超出写作职责", "coder", task.task());
                    return new Envelope(Envelope.CURRENT_VERSION, req.messageId(),
                            "writer", "coder", MessageType.HANDOFF, codec.toJson(handoff));
                }
                Payloads.TaskResult result = new Payloads.TaskResult(
                        req.messageId(), "writer 完成文章: " + task.task(), true);
                return new Envelope(Envelope.CURRENT_VERSION, req.messageId(),
                        "writer", req.from(), MessageType.RESULT, codec.toJson(result));
            }
        };
    }

    /** coder：执行代码任务。 */
    static TeamMember coder() {
        return new TeamMember() {
            @Override
            public String name() {
                return "coder";
            }

            @Override
            public Envelope handle(Envelope req, ProtocolCodec codec) {
                Payloads.TaskRequest task =
                        (Payloads.TaskRequest) codec.parsePayload(MessageType.REQUEST, req.payload());
                Payloads.TaskResult result = new Payloads.TaskResult(
                        req.messageId(), "coder 完成代码: " + task.task(), true);
                return new Envelope(Envelope.CURRENT_VERSION, req.messageId(),
                        "coder", req.from(), MessageType.RESULT, codec.toJson(result));
            }
        };
    }

    public static void main(String[] args) {
        System.out.println("=== S3.3 Agent 间通信协议 Demo（离线）===");
        AgentProtocol protocol = new AgentProtocol();
        protocol.register(writer());
        protocol.register(coder());
        ProtocolCodec codec = protocol.codec();

        // 1. 文章任务：writer 自己做
        Envelope article = new Envelope(Envelope.CURRENT_VERSION, "m1", "user", "writer",
                MessageType.REQUEST, codec.toJson(new Payloads.TaskRequest("写周报", "markdown")));
        print(protocol.send(article));

        // 2. 代码任务：writer handoff 给 coder（由总线再次路由到 coder）
        Envelope code = new Envelope(Envelope.CURRENT_VERSION, "m2", "user", "writer",
                MessageType.REQUEST, codec.toJson(new Payloads.TaskRequest("写 Java 代码", "java")));
        Envelope handoff = protocol.send(code);
        print(handoff);
        if (handoff.type() == MessageType.HANDOFF) {
            Payloads.Handoff h = (Payloads.Handoff) codec.parsePayload(MessageType.HANDOFF, handoff.payload());
            Envelope forwarded = new Envelope(Envelope.CURRENT_VERSION, h.taskId(),
                    "writer", h.forwardTo(), MessageType.REQUEST,
                    codec.toJson(new Payloads.TaskRequest(h.context(), "java")));
            print(protocol.send(forwarded));
        }

        // 3. 不合法消息：未知接收方
        Envelope bad = new Envelope(Envelope.CURRENT_VERSION, "m3", "user", "nobody",
                MessageType.REQUEST, "{}");
        print(protocol.send(bad));
    }

    static void print(Envelope env) {
        System.out.println("[" + env.type() + "] " + env.from() + " → " + env.to() + " : " + env.payload());
    }
}
