package com.example.agentlab.permission;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

/** 执行节点：只有 approved=true 才真正执行操作。  * @author guoxiangyue
 */
public class ExecuteNode implements NodeAction<ApprovalState> {

    @Override
    public Map<String, Object> apply(ApprovalState state) {
        String operation = state.<String>value(ApprovalState.OPERATION_KEY).orElse("未知操作");
        boolean approved = Boolean.TRUE.equals(state.<Boolean>value(ApprovalState.APPROVED_KEY).orElse(false));

        if (!approved) {
            // 防御：即使被错误路由到这里，未批准也绝不执行
            return Map.of(ApprovalState.LOG_KEY, "未执行（缺少审批）: " + operation);
        }
        return Map.of(ApprovalState.LOG_KEY, "已执行操作: " + operation);
    }
}
