package com.example.agentlab.supervisor;

import com.example.agentlab.common.ModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * LLM 主管路由器：结构化输出路由决策。
  * @author guoxiangyue
 */
public class LlmRouter implements Router {

    interface Agent {
        @SystemMessage("""
                你是 Supervisor 主管。根据任务和已完成结果，判断下一步派给谁：
                - writer：写作类工作（写文档、周报、报告）
                - coder：代码类工作（写代码、改 bug）
                - analyst：数据分析类工作（统计、图表、数据洞察）
                - done：任务已全部完成
                规则：一次只派一个 worker；已完成的结果不要重复派；结果已覆盖全部需求时返回 done。""")
        RouteDecision route(@UserMessage String taskAndResults);
    }

    private final Agent agent;

    public LlmRouter(ChatModel model) {
        this.agent = AiServices.builder(Agent.class).chatModel(model).build();
    }

    @Override
    public RouteDecision route(String task, List<String> results) {
        String context = "任务: " + task + "\n已完成结果: " + String.join(" | ", results);
        return agent.route(context);
    }
}
