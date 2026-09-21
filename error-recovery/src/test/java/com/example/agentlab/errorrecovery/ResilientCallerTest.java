package com.example.agentlab.errorrecovery;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 韧性调用器：三种恢复路径的编排逻辑。
 */
class ResilientCallerTest {

    /** 桩：不真睡。 */
    private static final ResilientCaller.Sleeper NO_SLEEP = ms -> {
    };

    private ResilientCaller caller() {
        return new ResilientCaller(5, 3, NO_SLEEP, new Random(1));
    }

    private static ResilientCaller.RecoveryActions noopActions() {
        return new ResilientCaller.RecoveryActions() {
            @Override
            public void onReactiveCompact() {
            }

            @Override
            public void onSwitchFallback() {
            }
        };
    }

    @Test
    void retryAfterRateLimit_eventuallySucceeds() {
        AtomicInteger calls = new AtomicInteger();
        String result = caller().call(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new RuntimeException("429 rate limit");
            }
            return "success";
        }, noopActions());

        assertThat(result).isEqualTo("success");
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void exceedsMaxRetries_throwsRecoveryExhausted() {
        assertThatThrownBy(() -> caller().call(
                () -> {
                    throw new RuntimeException("429 rate limit");
                }, noopActions()))
                .isInstanceOf(RecoveryExhaustedException.class)
                .hasMessageContaining("重试 5 次");
    }

    @Test
    void consecutiveOverload_switchesFallback() {
        List<String> switches = new ArrayList<>();
        ResilientCaller.RecoveryActions actions = new ResilientCaller.RecoveryActions() {
            @Override
            public void onReactiveCompact() {
            }

            @Override
            public void onSwitchFallback() {
                switches.add("fallback");
            }
        };

        assertThatThrownBy(() -> caller().call(
                () -> {
                    throw new RuntimeException("529 overloaded");
                }, actions))
                .isInstanceOf(RecoveryExhaustedException.class);

        // 连续 3 次 529 → 切一次 fallback（5 次重试内至少触发一次）
        assertThat(switches).isNotEmpty();
    }

    @Test
    void promptTooLong_compactsOnce_thenSucceeds() {
        AtomicInteger compactions = new AtomicInteger();
        ResilientCaller.RecoveryActions actions = new ResilientCaller.RecoveryActions() {
            @Override
            public void onReactiveCompact() {
                compactions.incrementAndGet();
            }

            @Override
            public void onSwitchFallback() {
            }
        };
        AtomicInteger calls = new AtomicInteger();

        String result = caller().call(() -> {
            if (calls.incrementAndGet() == 1) {
                throw new RuntimeException("413 prompt too long");
            }
            return "after-compact";
        }, actions);

        assertThat(result).isEqualTo("after-compact");
        assertThat(compactions.get()).isEqualTo(1);
    }

    @Test
    void promptTooLong_compactsButStillFails_throws() {
        AtomicInteger compactions = new AtomicInteger();
        ResilientCaller.RecoveryActions actions = new ResilientCaller.RecoveryActions() {
            @Override
            public void onReactiveCompact() {
                compactions.incrementAndGet();
            }

            @Override
            public void onSwitchFallback() {
            }
        };

        assertThatThrownBy(() -> caller().call(
                () -> {
                    throw new RuntimeException("413 prompt too long");
                }, actions))
                .isInstanceOf(RecoveryExhaustedException.class)
                .hasMessageContaining("已压缩过");
        assertThat(compactions.get()).isEqualTo(1);
    }

    @Test
    void otherError_propagatesImmediately() {
        assertThatThrownBy(() -> caller().call(
                () -> {
                    throw new RuntimeException("server crash");
                }, noopActions()))
                .hasMessageContaining("server crash")
                .isNotInstanceOf(RecoveryExhaustedException.class);
    }
}
