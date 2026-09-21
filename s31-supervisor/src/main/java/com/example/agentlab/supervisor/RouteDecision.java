package com.example.agentlab.supervisor;

/**
 * 路由决策：主管判断下一步派给哪个 worker（或完成）。
 *
 * @param worker "writer" / "coder" / "analyst" / "done"
 * @param reason 路由理由
  * @author guoxiangyue
 */
public record RouteDecision(String worker, String reason) {

    public static RouteDecision done(String reason) {
        return new RouteDecision("done", reason);
    }

    public boolean isDone() {
        return "done".equalsIgnoreCase(worker);
    }
}
