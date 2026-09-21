package com.example.agentlab.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 关键词选择器离线测试（LLM side-query 的降级路径）。
 */
class KeywordMemorySelectorTest {

    private final KeywordMemorySelector selector = new KeywordMemorySelector();

    @Test
    void selects_matchingMemory() {
        List<MemoryRecord> all = List.of(
                MemoryRecord.of("pref-tabs", "用户偏好 tab 缩进", MemoryType.USER, "用 tab 不用空格"),
                MemoryRecord.of("project-auth", "认证模块重写进行中", MemoryType.PROJECT, "auth 重写是合规驱动")
        );

        List<MemoryRecord> selected = selector.select(all, "用户: 继续用 tab 写代码", 5);

        assertThat(selected).extracting(MemoryRecord::name).containsExactly("pref-tabs");
    }

    @Test
    void noMatch_returnsEmpty() {
        List<MemoryRecord> all = List.of(
                MemoryRecord.of("pref-tabs", "用户偏好 tab 缩进", MemoryType.USER, "用 tab")
        );

        assertThat(selector.select(all, "用户: 今天天气不错", 5)).isEmpty();
    }

    @Test
    void respectsMaxItems() {
        List<MemoryRecord> all = List.of(
                MemoryRecord.of("m1", "关于 排序算法 的偏好", MemoryType.USER, "a"),
                MemoryRecord.of("m2", "关于 排序算法 的反馈", MemoryType.FEEDBACK, "b"),
                MemoryRecord.of("m3", "关于 排序算法 的项目事实", MemoryType.PROJECT, "c")
        );

        List<MemoryRecord> selected = selector.select(all, "用户: 排序算法怎么优化", 2);

        assertThat(selected).hasSize(2);
    }
}
