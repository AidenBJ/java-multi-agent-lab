package com.example.agentlab.supervisor;

import java.util.List;

/**
 * 主管路由器：根据任务和已有结果，决定下一步派给谁。
  * @author guoxiangyue
 */
public interface Router {

    RouteDecision route(String task, List<String> results);
}
