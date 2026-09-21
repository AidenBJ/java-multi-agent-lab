package com.example.agentlab.subagent;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * langgraph4j 子图演示用共享状态：in（输入）/ out（输出）为 overwrite 字段，messages 追加。
  * @author guoxiangyue
 */
public class WorkState extends AgentState {

    public static final String IN = "in";
    public static final String OUT = "out";
    public static final String MESSAGES = "messages";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES, Channels.appender(ArrayList::new)
    );

    public WorkState(Map<String, Object> initData) {
        super(initData);
    }

    public Optional<String> in() {
        return value(IN);
    }

    public Optional<String> out() {
        return value(OUT);
    }

    public List<String> messages() {
        return this.<List<String>>value(MESSAGES).orElse(List.of());
    }
}
