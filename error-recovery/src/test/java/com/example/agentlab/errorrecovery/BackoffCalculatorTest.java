package com.example.agentlab.errorrecovery;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 指数退避：min(500×2^attempt, 32000) + jitter(0~25%)。
 */
class BackoffCalculatorTest {

    @Test
    void firstAttempt_around500() {
        long delay = BackoffCalculator.delayMillis(0, new Random(1));
        assertThat(delay).isBetween(BackoffCalculator.BASE_DELAY_MS,
                (long) (BackoffCalculator.BASE_DELAY_MS * 1.25));
    }

    @Test
    void secondAttempt_around1000() {
        long delay = BackoffCalculator.delayMillis(1, new Random(1));
        assertThat(delay).isBetween(1000L, 1250L);
    }

    @Test
    void cappedAtMaxDelay() {
        // attempt 10：500 × 2^10 = 512000 → 截断到 32000
        long delay = BackoffCalculator.delayMillis(10, new Random(1));
        assertThat(delay).isBetween(BackoffCalculator.MAX_DELAY_MS,
                (long) (BackoffCalculator.MAX_DELAY_MS * 1.25));
    }
}
