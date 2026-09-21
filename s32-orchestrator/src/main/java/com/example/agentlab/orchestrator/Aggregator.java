package com.example.agentlab.orchestrator;

import java.util.List;

/**
 * 聚合器：把所有 worker 的结果汇总成最终报告。
  * @author guoxiangyue
 */
public interface Aggregator {

    String aggregate(String task, List<String> partialResults);
}
