package com.example.agentlab.supervisor;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Supervisor 图状态：任务 + 当前路由 + 各 worker 累积的结果。
  * @author guoxiangyue
 */
public class SupervisorState extends AgentState {

    /** results 追加式；task/route 未声明，默认 overwrite（langgraph4j 1.8.27 实证）。 */
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            "results", Channels.appender(ArrayList::new)
    );

    public SupervisorState(Map<String, Object> data) {
        super(data);
    }

    public String task() {
        return this.<String>value("task").orElse("");
    }

    public String route() {
        return this.<String>value("route").orElse("supervisor");
    }

    @SuppressWarnings("unchecked")
    public List<String> results() {
        return this.<List<String>>value("results").orElse(List.of());
    }
}
