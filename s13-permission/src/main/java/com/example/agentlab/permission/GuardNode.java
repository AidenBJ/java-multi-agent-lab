package com.example.agentlab.permission;

import org.bsc.langgraph4j.action.NodeAction;

import java.util.Map;

/** 守卫节点：把"这次操作是什么、是否需要审批"写入共享状态。  * @author guoxiangyue
 */
public class GuardNode implements NodeAction<ApprovalState> {

    @Override
    public Map<String, Object> apply(ApprovalState state) {
        return Map.of(
                ApprovalState.OPERATION_KEY, "删除文件 report.txt（破坏性操作）",
                ApprovalState.NEEDS_APPROVAL_KEY, true
        );
    }
}
