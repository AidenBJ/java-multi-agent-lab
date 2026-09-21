package com.example.agentlab.errorrecovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * S2.5 离线 Demo：用桩异常演示三条恢复路径的轨迹（不真等、不调 LLM）。
 *
 * <pre>
 *   mvn -q -pl error-recovery exec:java "-Dexec.mainClass=com.example.agentlab.errorrecovery.RecoveryDemo"
 * </pre>
  * @author guoxiangyue
 */
public class RecoveryDemo {

    /** 桩睡眠器：记录延迟值。 */
    static class LoggingSleeper implements ResilientCaller.Sleeper {
        final List<Long> delays = new ArrayList<>();

        @Override
        public void sleep(long ms) {
            delays.add(ms);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== S2.5 错误恢复 Demo（离线）===");

        // 场景 1：429 连续 2 次后成功 → 指数退避重试
        LoggingSleeper sleeper = new LoggingSleeper();
        ResilientCaller caller = new ResilientCaller(10, 3, sleeper, new Random(42));
        int[] counter = {0};
        String result1 = caller.call(() -> {
            if (counter[0]++ < 2) {
                throw new RuntimeException("429 Too Many Requests");
            }
            return "ok-after-retry";
        }, new ResilientCaller.RecoveryActions() {
            @Override
            public void onReactiveCompact() {
            }

            @Override
            public void onSwitchFallback() {
            }
        });
        System.out.println("[场景1] 429×2 → 成功: " + result1 + "；退避延迟: " + sleeper.delays + "ms");

        // 场景 2：连续 529 过载 3 次 → 触发切换备用模型
        List<String> switches = new ArrayList<>();
        ResilientCaller caller2 = new ResilientCaller(10, 3, sleeper, new Random(42));
        try {
            caller2.call(() -> {
                throw new RuntimeException("529 Overloaded");
            }, new ResilientCaller.RecoveryActions() {
                @Override
                public void onReactiveCompact() {
                }

                @Override
                public void onSwitchFallback() {
                    switches.add("switched");
                }
            });
        } catch (RecoveryExhaustedException e) {
            System.out.println("[场景2] 连续 529：切换备用模型 " + switches.size() + " 次，最终: " + e.getMessage());
        }

        // 场景 3：prompt_too_long → 压缩后重试成功
        List<String> compactions = new ArrayList<>();
        ResilientCaller caller3 = new ResilientCaller(10, 3, sleeper, new Random(42));
        int[] compactCount = {0};
        String result3 = caller3.call(() -> {
            if (compactCount[0]++ == 0) {
                throw new RuntimeException("413 prompt too long");
            }
            return "ok-after-compact";
        }, new ResilientCaller.RecoveryActions() {
            @Override
            public void onReactiveCompact() {
                compactions.add("compacted");
            }

            @Override
            public void onSwitchFallback() {
            }
        });
        System.out.println("[场景3] prompt_too_long → 压缩 " + compactions.size() + " 次后重试: " + result3);
    }
}
