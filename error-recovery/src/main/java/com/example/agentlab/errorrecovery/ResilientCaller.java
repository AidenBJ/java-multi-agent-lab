package com.example.agentlab.errorrecovery;

import java.util.Random;
import java.util.function.Supplier;

/**
 * 韧性调用器：把一次可能失败的 LLM 调用包成"自动恢复"的调用。
 *
 * <p>对齐 s11 的三条路径：</p>
 * <ul>
 *   <li>429/529 → 指数退避重试（最多 maxRetries 次）；连续 overloadThreshold 次 529 → 切备用模型回调；</li>
 *   <li>prompt_too_long → 调 reactiveCompact 回调压缩后重试一次；再超就放弃；</li>
 *   <li>OTHER → 直接抛出。</li>
 * </ul>
  * @author guoxiangyue
 */
public class ResilientCaller {

    /** 可注入的睡眠器（测试不真等）。 */
    public interface Sleeper {
        void sleep(long ms) throws InterruptedException;
    }

    /** 恢复动作回调。 */
    public interface RecoveryActions {
        /** prompt_too_long 时：压缩上下文。 */
        void onReactiveCompact();

        /** 连续过载达阈值时：切换备用模型。 */
        void onSwitchFallback();
    }

    private final int maxRetries;
    private final int overloadThreshold;
    private final Sleeper sleeper;
    private final Random rng;

    public ResilientCaller(int maxRetries, int overloadThreshold, Sleeper sleeper, Random rng) {
        this.maxRetries = maxRetries;
        this.overloadThreshold = overloadThreshold;
        this.sleeper = sleeper;
        this.rng = rng;
    }

    /** 真实版本：真睡毫秒。 */
    public static ResilientCaller defaultCaller() {
        return new ResilientCaller(10, 3, ms -> Thread.sleep(ms), new Random());
    }

    public <T> T call(Supplier<T> action, RecoveryActions actions) {
        int attempt = 0;
        int consecutiveOverload = 0;
        boolean compacted = false;

        while (true) {
            try {
                return action.get();
            } catch (Exception e) {
                ErrorType type = ErrorClassifier.classify(e);
                switch (type) {
                    case PROMPT_TOO_LONG -> {
                        if (compacted) {
                            throw new RecoveryExhaustedException("上下文超限，且已压缩过一次仍失败", e);
                        }
                        actions.onReactiveCompact();
                        compacted = true;
                        continue;
                    }
                    case RATE_LIMITED, OVERLOADED -> {
                        if (attempt >= maxRetries) {
                            throw new RecoveryExhaustedException("重试 " + maxRetries + " 次后仍失败", e);
                        }
                        try {
                            sleeper.sleep(BackoffCalculator.delayMillis(attempt, rng));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RecoveryExhaustedException("退避等待被中断", ie);
                        }
                        attempt++;
                        if (type == ErrorType.OVERLOADED) {
                            consecutiveOverload++;
                            if (consecutiveOverload >= overloadThreshold) {
                                actions.onSwitchFallback();
                                consecutiveOverload = 0;
                            }
                        } else {
                            consecutiveOverload = 0;
                        }
                    }
                    default -> {
                        throw e;
                    }
                }
            }
        }
    }
}
