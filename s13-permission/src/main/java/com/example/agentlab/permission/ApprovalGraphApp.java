package com.example.agentlab.permission;

import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.Map;
import java.util.Scanner;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * S1.3 Demo：审批门——破坏性操作必须经用户确认才能执行。
 *
 * <p>流程：guard →（需要审批？）→ decision →（批准？）→ execute | denied</p>
 *
 * <pre>
 *   mvn -q -pl permission exec:java "-Dexec.mainClass=com.example.agentlab.permission.ApprovalGraphApp"
 * </pre>
  * @author guoxiangyue
 */
public class ApprovalGraphApp {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        var compiledGraph = new StateGraph<>(ApprovalState.SCHEMA, ApprovalState::new)
                .addNode("guard", node_async(new GuardNode()))
                .addNode("decision", node_async(new DecisionNode(() -> askUser(scanner))))
                .addNode("execute", node_async(new ExecuteNode()))
                .addNode("denied", node_async(new DeniedNode()))
                .addEdge(START, "guard")
                // guard → 需要审批则去 decision，否则直接 execute
                .addConditionalEdges("guard",
                        edge_async(state -> Boolean.TRUE.equals(
                                state.<Boolean>value(ApprovalState.NEEDS_APPROVAL_KEY).orElse(false))
                                ? "needs-approval" : "no-approval"),
                        Map.of("needs-approval", "decision", "no-approval", "execute"))
                // decision → 批准则 execute，否则 denied
                .addConditionalEdges("decision",
                        edge_async(state -> Boolean.TRUE.equals(
                                state.<Boolean>value(ApprovalState.APPROVED_KEY).orElse(false))
                                ? "approved" : "rejected"),
                        Map.of("approved", "execute", "rejected", "denied"))
                .addEdge("execute", END)
                .addEdge("denied", END)
                .compile();

        System.out.println("== 审批门 Demo ==");
        ApprovalState result = compiledGraph.invoke(Map.of()).get();
        System.out.println("执行轨迹: " + result.log());
    }

    /** 从控制台读取 y/N；默认拒绝（未确认绝不执行）。 */
    private static boolean askUser(Scanner scanner) {
        System.out.print("操作需要审批，是否批准执行？(y/N): ");
        if (!scanner.hasNextLine()) {
            return false;
        }
        return scanner.nextLine().trim().equalsIgnoreCase("y");
    }
}
