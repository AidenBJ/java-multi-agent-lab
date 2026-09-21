package com.example.agentlab.protocol;

/**
 * 各 payload 结构（协议的"消息 Schema"）。
  * @author guoxiangyue
 */
public final class Payloads {

    private Payloads() {
    }

    /** 任务请求。 */
    public record TaskRequest(String task, String expectedFormat) {
    }

    /** 任务结果。 */
    public record TaskResult(String taskId, String content, boolean ok) {
    }

    /** 标准错误。 */
    public record TaskError(ErrorCode code, String detail, boolean retryable) {
    }

    /** Handoff：明确交给谁、为什么。 */
    public record Handoff(String taskId, String reason, String forwardTo, String context) {
    }
}
