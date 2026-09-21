package com.example.agentlab.permission;

import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * S1.3 审批门共享状态。
 *
 * <ul>
 *   <li>operation      —— 待审批的操作描述（overwrite）</li>
 *   <li>needsApproval  —— 是否需要审批（overwrite）</li>
 *   <li>approved       —— 审批决定（overwrite）</li>
 *   <li>log            —— 执行轨迹（append）</li>
 * </ul>
 * 未声明的字段走 langgraph4j 默认 overwrite reducer。
  * @author guoxiangyue
 */
public class ApprovalState extends AgentState {

    public static final String OPERATION_KEY = "operation";
    public static final String NEEDS_APPROVAL_KEY = "needsApproval";
    public static final String APPROVED_KEY = "approved";
    public static final String LOG_KEY = "log";

    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            LOG_KEY, Channels.appender(ArrayList::new)
    );

    public ApprovalState(Map<String, Object> initData) {
        super(initData);
    }

    public List<String> log() {
        return this.<List<String>>value(LOG_KEY).orElse(List.of());
    }
}
