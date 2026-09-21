package com.example.agentlab.subagent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 报告汇总员：把多个子任务的结果整合成最终报告。
  * @author guoxiangyue
 */
public interface ResultAggregator {

    @SystemMessage("""
            你是报告汇总员。把多个子任务的结果整合成一份连贯、结构化的最终报告：
            1. 开头给出总目标与执行概述；
            2. 按子任务分节整合关键信息，不要遗漏任何子任务；
            3. 结尾给出综合结论。""")
    String aggregate(@UserMessage String rawResults);
}
