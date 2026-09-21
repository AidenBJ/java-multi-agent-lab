package com.example.agentlab.permission;

import org.bsc.langgraph4j.StateGraph;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 审批门纯逻辑测试：验证"未确认绝不执行"的安全语义。
 */
class ApprovalGraphTest {

    private StateGraph<ApprovalState> graph() throws Exception {
        return new StateGraph<>(ApprovalState.SCHEMA, ApprovalState::new)
                .addNode("guard", node_async(new GuardNode()))
                .addNode("execute", node_async(new ExecuteNode()))
                .addNode("denied", node_async(new DeniedNode()))
                .addEdge(START, "guard")
                .addConditionalEdges("guard",
                        edge_async(state -> Boolean.TRUE.equals(
                                state.<Boolean>value(ApprovalState.NEEDS_APPROVAL_KEY).orElse(false))
                                ? "needs-approval" : "no-approval"),
                        Map.of("needs-approval", "denied", "no-approval", "execute"))
                .addEdge("denied", END)
                .addEdge("execute", END);
    }

    @Test
    void unapprovedOperation_isNeverExecuted() throws Exception {
        // 拒绝场景：不注入任何决定 → 走 denied 分支
        ApprovalState result = graph().compile().invoke(Map.of()).get();

        assertThat(result.log())
                .contains("已拒绝操作: 删除文件 report.txt（破坏性操作）")
                .doesNotContain("已执行");
    }

    @Test
    void approvedOperation_isExecuted() throws Exception {
        // 批准场景：注入决定后走 execute 分支
        var graph = new StateGraph<>(ApprovalState.SCHEMA, ApprovalState::new)
                .addNode("guard", node_async(new GuardNode()))
                .addNode("decision", node_async(new DecisionNode(() -> true)))
                .addNode("execute", node_async(new ExecuteNode()))
                .addNode("denied", node_async(new DeniedNode()))
                .addEdge(START, "guard")
                .addConditionalEdges("guard",
                        edge_async(state -> Boolean.TRUE.equals(
                                state.<Boolean>value(ApprovalState.NEEDS_APPROVAL_KEY).orElse(false))
                                ? "needs-approval" : "no-approval"),
                        Map.of("needs-approval", "decision", "no-approval", "execute"))
                .addConditionalEdges("decision",
                        edge_async(state -> Boolean.TRUE.equals(
                                state.<Boolean>value(ApprovalState.APPROVED_KEY).orElse(false))
                                ? "approved" : "rejected"),
                        Map.of("approved", "execute", "rejected", "denied"))
                .addEdge("execute", END)
                .addEdge("denied", END);

        ApprovalState result = graph.compile().invoke(Map.of()).get();

        assertThat(result.log()).contains("已执行操作: 删除文件 report.txt（破坏性操作）");
    }
}
