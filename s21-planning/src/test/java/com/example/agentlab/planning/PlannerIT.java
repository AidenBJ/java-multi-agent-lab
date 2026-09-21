package com.example.agentlab.planning;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实调用 DeepSeek 验证"结构化计划产出"：未设置 DEEPSEEK_API_KEY 时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class PlannerIT {

    @Test
    void planner_returnsStructuredPlan() {
        ChatModel model = ModelFactory.createDefaultChatModel();
        Planner planner = AiServices.builder(Planner.class)
                .chatModel(model)
                .build();

        Plan plan = planner.plan("为'写一篇技术博客'制定 3 步计划");

        assertThat(plan).isNotNull();
        assertThat(plan.goal()).isNotBlank();
        assertThat(plan.todos()).isNotEmpty();
        System.out.println("目标: " + plan.goal());
        System.out.println("步骤: " + plan.todos().size());
    }
}
