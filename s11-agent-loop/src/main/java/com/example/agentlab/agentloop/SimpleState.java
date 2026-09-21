package com.example.agentlab.agentloop;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * langgraph4j 对比示例：图共享状态（追加式消息列表）。
  * @author guoxiangyue
 */
public class SimpleState extends AgentState {

    public static final String MESSAGES_KEY = "messages";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new)
    );

    public SimpleState(Map<String, Object> initData) {
        super(initData);
    }

    public List<String> messages() {
        return this.<List<String>>value(MESSAGES_KEY).orElse(List.of());
    }
}
