package com.example.agentlab.errorrecovery;

import java.util.Random;

/**
 * 指数退避计算器（对齐 s11 withRetry）：
 * {@code delay = min(500 × 2^attempt, 32000) + random(0~25%)}。
  * @author guoxiangyue
 */
public final class BackoffCalculator {

    public static final long BASE_DELAY_MS = 500;
    public static final long MAX_DELAY_MS = 32_000;

    private BackoffCalculator() {
    }

    public static long delayMillis(int attempt, Random rng) {
        long base = Math.min(BASE_DELAY_MS * (1L << Math.min(attempt, 16)), MAX_DELAY_MS);
        double jitter = rng.nextDouble() * base * 0.25;
        return base + (long) jitter;
    }
}
