package com.example.agentlab.memory;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实调用 DeepSeek 验证 LLM 提取/选择链路：未设置 DEEPSEEK_API_KEY 时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class LlmMemoryIT {

    @Test
    void llmExtractor_extractsPreferenceFromDialogue() {
        ChatModel model = ModelFactory.createDefaultChatModel();
        LlmMemoryExtractor extractor = new LlmMemoryExtractor(model);

        List<NewMemory> memories = extractor.extract(
                "用户: 以后写代码都用 tab 缩进，不要用空格。\n助手: 好的，明白了。");

        assertThat(memories).isNotEmpty();
        System.out.println("提取到 " + memories.size() + " 条: " + memories.stream()
                .map(NewMemory::name).toList());
    }

    @Test
    void llmSelector_selectsRelevantMemory() {
        ChatModel model = ModelFactory.createDefaultChatModel();
        LlmMemorySelector selector = new LlmMemorySelector(model);
        List<MemoryRecord> all = List.of(
                MemoryRecord.of("pref-tabs", "用户偏好 tab 缩进", MemoryType.USER, "用 tab 不用空格"),
                MemoryRecord.of("project-auth", "认证模块重写进行中", MemoryType.PROJECT, "auth 重写是合规驱动")
        );

        List<MemoryRecord> selected = selector.select(all, "用户: 继续用 tab 写代码", 5);

        assertThat(selected).isNotEmpty();
        System.out.println("选中: " + selected.stream().map(MemoryRecord::name).toList());
    }
}
