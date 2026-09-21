package com.example.agentlab.hooks;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TraceObserver 记录逻辑测试（纯逻辑，无网络）。
 */
class TraceObserverTest {

    @Test
    void observer_recordsEventsAndToolTraces() {
        TraceObserver observer = new TraceObserver();

        observer.onIterationStart(0);
        observer.onModelResponse(0, "", true);
        observer.onToolStart("now", "{}");
        observer.onToolEnd("now", "当前时间 12:00", 5);
        observer.onIterationStart(1);
        observer.onModelResponse(1, "现在是 12:00", false);
        observer.onLoopFinished("现在是 12:00");

        assertThat(observer.iterationCount()).isEqualTo(2);
        assertThat(observer.toolTraces()).hasSize(1);
        assertThat(observer.toolTraces().get(0).name()).isEqualTo("now");
        assertThat(observer.toolTraces().get(0).result()).isEqualTo("当前时间 12:00");
        assertThat(observer.finalAnswer()).isEqualTo("现在是 12:00");
        assertThat(observer.eventLog()).hasSize(7);
    }

    @Test
    void report_containsKeySections() {
        TraceObserver observer = new TraceObserver();
        observer.onIterationStart(0);
        observer.onToolStart("echo", "hello");
        observer.onToolEnd("echo", "你说了: hello", 1);
        observer.onLoopFinished("done");

        String report = observer.report();

        assertThat(report)
                .contains("=== Agent 执行轨迹报告 ===")
                .contains("迭代轮数: 1")
                .contains("工具调用次数: 1")
                .contains("echo")
                .contains("最终答案: done");
    }
}
