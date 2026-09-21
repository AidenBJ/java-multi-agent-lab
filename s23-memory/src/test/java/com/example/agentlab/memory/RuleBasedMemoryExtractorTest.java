package com.example.agentlab.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 规则提取器离线测试（无 LLM 的降级路径）。
 */
class RuleBasedMemoryExtractorTest {

    private final RuleBasedMemoryExtractor extractor = new RuleBasedMemoryExtractor();

    @Test
    void extracts_explicitPreference() {
        List<NewMemory> memories = extractor.extract("用户: 记住：用 tab 缩进不要用空格");

        assertThat(memories).hasSize(1);
        assertThat(memories.get(0).type()).isEqualTo(MemoryType.USER);
        assertThat(memories.get(0).description()).contains("tab");
    }

    @Test
    void extracts_likePreference() {
        List<NewMemory> memories = extractor.extract("用户: 我喜欢深色模式");

        assertThat(memories).hasSize(1);
        assertThat(memories.get(0).description()).contains("深色模式");
    }

    @Test
    void extracts_futureUsePreference() {
        List<NewMemory> memories = extractor.extract("用户: 以后都用单引号");

        assertThat(memories).hasSize(1);
        assertThat(memories.get(0).description()).contains("单引号");
    }

    @Test
    void ignoresOrdinaryDialogue() {
        List<NewMemory> memories = extractor.extract("用户: 帮我写个排序算法\n助手: 好的，下面是归并排序的 Java 实现");

        assertThat(memories).isEmpty();
    }

    @Test
    void eachLine_producesAtMostOne() {
        List<NewMemory> memories = extractor.extract("记住：A 方案\n用户: 记住：B 方案");

        assertThat(memories).hasSize(2);
    }
}
