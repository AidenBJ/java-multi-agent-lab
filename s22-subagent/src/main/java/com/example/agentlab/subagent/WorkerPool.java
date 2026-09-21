package com.example.agentlab.subagent;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工人池：workerId → 独立 WorkerAgent 实例。
 *
 * <p>每个工人用独立的 AiServices 实例 + 各自系统提示构建，没有共享记忆——
 * 这就是"上下文隔离"的实现点。</p>
  * @author guoxiangyue
 */
public class WorkerPool {

    private final Map<String, WorkerAgent> workers = new HashMap<>();

    /** 注册一个工人（真实模型实例）。 */
    public void register(String workerId, String rolePrompt, ChatModel model) {
        WorkerAgent agent = AiServices.builder(WorkerAgent.class)
                .chatModel(model)
                .systemMessage(rolePrompt)
                .build();
        workers.put(workerId, agent);
    }

    /** 注册一个工人（测试桩注入）。 */
    public void register(String workerId, WorkerAgent agent) {
        workers.put(workerId, agent);
    }

    public WorkerAgent get(String workerId) {
        return workers.get(workerId);
    }

    public List<String> workerIds() {
        return List.copyOf(workers.keySet());
    }

    public int size() {
        return workers.size();
    }

    /** 创建标准三工人池（行业调研 / 代码 / 写作）。 */
    public static WorkerPool createStandard(ChatModel model) {
        WorkerPool pool = new WorkerPool();
        pool.register("research",
                "你是行业调研专家：擅长资料收集、趋势判断、结构化输出，回答用要点，不超过 300 字。", model);
        pool.register("coding",
                "你是资深 Java 工程师：擅长技术方案、代码实现与架构评估，回答给出关键代码或要点，不超过 300 字。", model);
        pool.register("writing",
                "你是中文写作专家：擅长把材料组织成流畅、专业的文章，直接输出成稿，不超过 400 字。", model);
        return pool;
    }
}
