package com.example.agentlab.permission;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

/** 拒绝节点：记录拒绝结果。  * @author guoxiangyue
 */
public class DeniedNode implements NodeAction<ApprovalState> {

    @Override
    public Map<String, Object> apply(ApprovalState state) {
        String operation = state.<String>value(ApprovalState.OPERATION_KEY).orElse("未知操作");
        return Map.of(ApprovalState.LOG_KEY, "已拒绝操作: " + operation);
    }
}
