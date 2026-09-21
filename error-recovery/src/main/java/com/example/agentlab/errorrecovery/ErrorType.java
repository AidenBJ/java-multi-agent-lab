package com.example.agentlab.errorrecovery;

/**
 * 错误类型（对齐 s11 的三种最常见故障模式）。
  * @author guoxiangyue
 */
public enum ErrorType {
    /** 正常结果（非错误）。 */
    OK,
    /** 输出被截断（max_tokens 用完）→ 升级 token / 续写。 */
    OUTPUT_TRUNCATED,
    /** 上下文超限（prompt_too_long）→ reactive compact 后重试。 */
    PROMPT_TOO_LONG,
    /** 限流（429）→ 指数退避重试。 */
    RATE_LIMITED,
    /** 过载（529）→ 指数退避；连续 N 次切备用模型。 */
    OVERLOADED,
    /** 其他不可恢复错误。 */
    OTHER
}
