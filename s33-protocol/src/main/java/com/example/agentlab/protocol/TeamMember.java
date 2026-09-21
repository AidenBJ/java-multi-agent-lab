package com.example.agentlab.protocol;

/**
 * 团队成员：严格按协议处理请求，返回 RESULT/ERROR/HANDOFF 信封。
  * @author guoxiangyue
 */
public interface TeamMember {

    String name();

    /**
     * 处理一条请求信封，返回响应信封（RESULT / ERROR / HANDOFF）。
     * 实现不得返回自由文本，必须构造符合协议的 Envelope。
     */
    Envelope handle(Envelope request, ProtocolCodec codec);
}
