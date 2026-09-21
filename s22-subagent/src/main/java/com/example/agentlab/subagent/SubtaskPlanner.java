package com.example.agentlab.subagent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * 任务分解器：把总任务拆成"可派发"的子任务计划。
 *
 * <p>每个子任务的 instruction 必须自包含（工人只有这一条指令，看不到总任务外的任何上下文）。
 * workerId 必须在系统提示给出的工人列表内。</p>
  * @author guoxiangyue
 */
public interface SubtaskPlanner {

    @SystemMessage("""
            你是任务分解器。可用工人（workerId）与专长：
            - research：行业调研、资料收集
            - coding：代码实现、技术方案
            - writing：写作、文案、报告润色
            要求：
            1. 把任务拆成 2~4 个自包含的子任务，每个子任务指定合适的 workerId；
            2. 子任务 instruction 必须完整自包含（工人只看得到这一条指令）；
            3. 不要拆分出无法由上述工人完成的任务。""")
    SubtaskPlan split(@UserMessage String task);

    /** 供系统提示拼接工人列表用（可注入）。 */
    record WorkerCatalog(List<String> workerIds) {
    }
}
