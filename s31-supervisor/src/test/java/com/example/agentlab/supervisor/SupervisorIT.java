package com.example.agentlab.supervisor;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 真实调用 DeepSeek 验证 Supervisor 路由链路：未设置 DEEPSEEK_API_KEY 时自动跳过。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class SupervisorIT {

    @Test
    void supervisorRoutesToWorkers() throws Exception {
        ChatModel model = ModelFactory.createDefaultChatModel();
        SupervisorGraph graph = new SupervisorGraph(
                new LlmRouter(model),
                List.of(
                        new LlmWorker("writer", "技术写作专家", model),
                        new LlmWorker("coder", "Java 后端工程师", model),
                        new LlmWorker("analyst", "数据分析师", model)));

        List<String> results = graph.run("写一份带数据的产品周报");

        assertThat(results).isNotEmpty();
        results.forEach(r -> System.out.println("  - " + r));
    }
}
