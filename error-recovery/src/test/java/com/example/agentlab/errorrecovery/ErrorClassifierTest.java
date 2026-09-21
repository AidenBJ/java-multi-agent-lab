package com.example.agentlab.errorrecovery;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 错误分类器：按异常 message 识别类型。
 */
class ErrorClassifierTest {

    @Test
    void classifiesRateLimit() {
        assertThat(ErrorClassifier.classify(new RuntimeException("429 Too Many Requests")))
                .isEqualTo(ErrorType.RATE_LIMITED);
        assertThat(ErrorClassifier.classify(new RuntimeException("rate limit exceeded")))
                .isEqualTo(ErrorType.RATE_LIMITED);
    }

    @Test
    void classifiesOverloaded() {
        assertThat(ErrorClassifier.classify(new RuntimeException("529 Overloaded")))
                .isEqualTo(ErrorType.OVERLOADED);
    }

    @Test
    void classifiesPromptTooLong() {
        assertThat(ErrorClassifier.classify(new RuntimeException("413 prompt too long")))
                .isEqualTo(ErrorType.PROMPT_TOO_LONG);
        assertThat(ErrorClassifier.classify(new RuntimeException("context length exceeded")))
                .isEqualTo(ErrorType.PROMPT_TOO_LONG);
    }

    @Test
    void otherErrors_unclassified() {
        assertThat(ErrorClassifier.classify(new RuntimeException("server crash")))
                .isEqualTo(ErrorType.OTHER);
    }
}
