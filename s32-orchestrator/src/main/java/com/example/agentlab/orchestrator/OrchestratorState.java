package com.example.agentlab.orchestrator;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orchestrator-Worker 状态：subtasks（拆解出的子任务）、partialResults（各 worker 并行产出）、
 * finalReport（聚合后的最终报告）。
  * @author guoxiangyue
 */
public class OrchestratorState extends AgentState {

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            "subtasks", Channels.appender(ArrayList::new),
            "partialResults", Channels.appender(ArrayList::new)
    );

    public OrchestratorState(Map<String, Object> data) {
        super(data);
    }

    public String task() {
        return this.<String>value("task").orElse("");
    }

    @SuppressWarnings("unchecked")
    public List<String> subtasks() {
        return this.<List<String>>value("subtasks").orElse(List.of());
    }

    @SuppressWarnings("unchecked")
    public List<String> partialResults() {
        return this.<List<String>>value("partialResults").orElse(List.of());
    }

    public String finalReport() {
        return this.<String>value("finalReport").orElse("");
    }
}
