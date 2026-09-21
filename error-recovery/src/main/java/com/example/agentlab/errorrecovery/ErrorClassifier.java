package com.example.agentlab.errorrecovery;

/**
 * 错误分类器：从异常识别错误类型（教学版按 message 匹配状态码关键词）。
 *
 * <p>真实生产应按 SDK 具体异常类型精细化（CC 有 13+ reason code）；
 * 这里对齐教学版"只处理 429/529"的简化，同时把分类做成可测的纯函数。</p>
  * @author guoxiangyue
 */
public final class ErrorClassifier {

    private ErrorClassifier() {
    }

    public static ErrorType classify(Throwable t) {
        String message = t.getMessage() == null ? "" : t.getMessage().toLowerCase();
        if (message.contains("429") || message.contains("rate limit") || message.contains("too many requests")) {
            return ErrorType.RATE_LIMITED;
        }
        if (message.contains("529") || message.contains("overloaded") || message.contains("overload")) {
            return ErrorType.OVERLOADED;
        }
        if (message.contains("413") || message.contains("prompt too long")
                || message.contains("context length") || message.contains("too large")) {
            return ErrorType.PROMPT_TOO_LONG;
        }
        return ErrorType.OTHER;
    }
}
